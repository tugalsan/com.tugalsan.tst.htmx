package com.tugalsan.tst.htmx;

import com.tugalsan.api.log.server.TS_Log;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import j2html.tags.specialized.LiTag;
import j2html.tags.specialized.UlTag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static j2html.TagCreator.*;
/*
<!DOCTYPE html>
<html>
   <head>
      <script src="/webjars/htmx.org/1.9.10/dist/htmx.min.js"></script>
      <link rel="stylesheet" href="/style/pico.min.css">
   </head>
   <body>
      <div class="container">
         <h1>com.tugalsan.tst.htmx.Server</h1>
         <ul id="todo-list">
            <li hx-target="#todo-3229dd83-8948-4da5-8a5d-10c6bb8f58c1" hx-swap="outerHTML" id="todo-3229dd83-8948-4da5-8a5d-10c6bb8f58c1" style="display: flex; align-items: center;">
               <input style="flex-basis: 0; min-width: 20px" type="checkbox" hx-post="/todos/3229dd83-8948-4da5-8a5d-10c6bb8f58c1/toggle">
               <div hx-post="/todos/3229dd83-8948-4da5-8a5d-10c6bb8f58c1/edit" style="flex-grow: 1; cursor: text;">BuyMilk1</div>
            </li>
            <li hx-target="#todo-556e864a-66c8-42c9-ac00-47cd643fe3be" hx-swap="outerHTML" id="todo-556e864a-66c8-42c9-ac00-47cd643fe3be" style="display: flex; align-items: center;">
               <input style="flex-basis: 0; min-width: 20px" type="checkbox" hx-post="/todos/556e864a-66c8-42c9-ac00-47cd643fe3be/toggle">
               <div hx-post="/todos/556e864a-66c8-42c9-ac00-47cd643fe3be/edit" style="flex-grow: 1; cursor: text;">BuyMilk2</div>
            </li>
            <li style="list-style-type: none">
               <form style="display: flex;" hx-swap="outerHTML" hx-target="#todo-list" hx-post="/todos"><input required type="text" name="content"><input value="Add" type="submit"></form>
            </li>
         </ul>
      </div>
   </body>
</html>
*/
public class Server {

    final private static TS_Log d = TS_Log.of(Server.class);

    public static record Item(String id, String content, boolean completed) {

        public static Item of(String content) {
            return new Item(UUID.randomUUID().toString(), content, false);
        }

        public static Item dbFetch(LinkedHashMap<String, Item> db, String id) {
            return db.get(id);
        }

        public static void dbAdd(LinkedHashMap<String, Item> db, String content) {
            dbAdd(db, Item.of(content));
        }

        @Deprecated
        public static void dbAdd(LinkedHashMap<String, Item> db, Item item) {
            db.put(item.id, item);
        }

        public static void renderDb(Context ctx, LinkedHashMap<String, Item> db) {
            ctx.html(toHtmx(db).render());
        }

        public static void renderItem(Context ctx, Item item, boolean editing) {
            ctx.html(toHtmx(item, editing).render());
        }

    }

    public static void main(String... s) {
        //PREREQUESTS
        var POM_HTMX_VERSION = "1.9.10";
        var PAGE_TITLE = Server.class.getName();

        //DB INIT
        var db = new LinkedHashMap<String, Item>();
        Item.dbAdd(db, "BuyMilk");

        //SERVER INIT
        var server = Javalin.create(config -> {
            config.staticFiles.enableWebjars();
            config.staticFiles.add("public");
        });

        //SERVER Handler / 
        server.addHttpHandler(HandlerType.GET, "/", ctx -> {
            var content = html(
                    head(
                            script().withSrc("/webjars/htmx.org/%s/dist/htmx.min.js".formatted(POM_HTMX_VERSION)),
                            link().withRel("stylesheet").withHref("/style/pico.min.css")
                    ),
                    body(
                            div(
                                    h1(PAGE_TITLE),
                                    toHtmx(db)
                            ).withClass("container")
                    )
            );
            var rendered = "<!DOCTYPE html>\n" + content.render();
            ctx.header("Cache-Control", "no-store");
            ctx.html(rendered);
        });

        //SERVER Handler /todos (new)
        server.addHttpHandler(HandlerType.POST, "/todos", ctx -> {
            Item.dbAdd(db, ctx.formParam("content"));
            Item.renderDb(ctx, db);
        });

        //SERVER Handler /todos/{id} (update)
        server.addHttpHandler(HandlerType.POST, "/todos/{id}", ctx -> {
            var id = ctx.pathParam("id");
            var itemUpdated = db.computeIfPresent(id, (_id, oldTodo)
                    -> new Item(id, ctx.formParam("value"), oldTodo.completed)
            );
            Item.renderItem(ctx, itemUpdated, false);
        });

        //SERVER Handler /todos/{id}/toggle
        server.addHttpHandler(HandlerType.POST, "/todos/{id}/toggle", ctx -> {
            var id = ctx.pathParam("id");
            var itemUpdated = db.computeIfPresent(id, (_id, oldTodo)
                    -> new Item(id, oldTodo.content, !oldTodo.completed)
            );
            Item.renderItem(ctx, itemUpdated, false);
        });

        //SERVER Handler /todos/{id}/edit
        server.addHttpHandler(HandlerType.POST, "/todos/{id}/edit", ctx -> {
            Item.renderItem(ctx, Item.dbFetch(db, ctx.pathParam("id")), true);
        });

        //SERVER Log
        server.after((ctx) -> d.cr("main", ctx.req().getMethod(), ctx.path(), ctx.status()));
        server.start();//server.stop();
    }

    private static UlTag toHtmx(Map<String, Item> db) {
        return ul()
                .withId("todo-list")
                .with(
                        db.values().stream()
                                .map(todo -> toHtmx(todo, false))
                )
                .with(
                        li(
                                form(
                                        input()
                                                .isRequired()
                                                .withType("text")
                                                .withName("content"),
                                        input()
                                                .withValue("Add")
                                                .withType("submit")
                                )
                                        .withStyle("display: flex;")
                                        .attr("hx-swap", "outerHTML")
                                        .attr("hx-target", "#todo-list")
                                        .attr("hx-post", "/todos")
                        )
                                .withStyle("list-style-type: none")
                );
    }

    private static LiTag toHtmx(Item todo, boolean editing) {
        var text = div(todo.content)
                .attr("hx-post", "/todos/" + todo.id + "/edit")
                .withStyle("flex-grow: 1; cursor: text;")
                .withCondStyle(todo.completed, "text-decoration: line-through; flex-grow: 1; cursor: text;");
        var editInput = input()
                .withValue(todo.content)
                .withName("value")
                .withType("text")
                .withStyle("flex-grow: 2;")
                .isAutofocus()
                .attr("hx-post", "/todos/" + todo.id)
                .attr("hx-target", "#todo-" + todo.id);
        var completeCheckbox = (todo.completed
                ? input()
                        .isChecked()
                : input())
                .withStyle("flex-basis: 0; min-width: 20px")
                .withType("checkbox")
                .withCondDisabled(editing)
                .attr("hx-post", "/todos/" + todo.id + "/toggle");
        return li(
                completeCheckbox,
                editing ? editInput : text
        )
                .attr("hx-target", "#todo-" + todo.id)
                .attr("hx-swap", "outerHTML")
                .withId("todo-" + todo.id)
                .withStyle("display: flex; align-items: center;");
    }
}
