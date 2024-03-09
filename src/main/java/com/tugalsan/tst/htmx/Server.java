package com.tugalsan.tst.htmx;

import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import j2html.tags.specialized.LiTag;
import j2html.tags.specialized.UlTag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static j2html.TagCreator.*;

public class Server {

    public static record Record(String id, String content, boolean completed) {

        public static Record newItem(String content) {
            return new Record(UUID.randomUUID().toString(), content, false);
        }
    }
    final private static TS_Log d = TS_Log.of(Server.class);
    final public static TS_ThreadSyncTrigger killTrigger = TS_ThreadSyncTrigger.of();
    public static Javalin server;

    public static void main(String... s) {
        //DB INIT
        var recordFirst = Record.newItem("buyMilk");
        var recordMap = new LinkedHashMap<String, Record>();
        recordMap.put(recordFirst.id, recordFirst);

        //SERVER INIT
        server = Javalin.create(config -> {
            config.staticFiles.enableWebjars();
            config.staticFiles.add("public");
        });

        //SERVER Handler / 
        server.addHttpHandler(HandlerType.GET, "/", ctx -> {
            var content = html(
                    head(
                            script().withSrc("/webjars/htmx.org/1.9.2/dist/htmx.min.js"),
                            link().withRel("stylesheet").withHref("/style/pico.min.css")
                    ),
                    body(
                            div(
                                    h1("Page Title"),
                                    toHtmx(recordMap)
                            ).withClass("container")
                    )
            );
            var rendered = "<!DOCTYPE html>\n" + content.render();
            ctx.header("Cache-Control", "no-store");
            ctx.html(rendered);
        });

        //SERVER Handler /todos (new)
        server.addHttpHandler(HandlerType.POST, "/todos", ctx -> {
            var newContent = ctx.formParam("content");
            var newTodo = Record.newItem(newContent);
            recordMap.put(newTodo.id, newTodo);
            ctx.html(toHtmx(recordMap).render());
        });

        //SERVER Handler /todos/{id} (update)
        server.addHttpHandler(HandlerType.POST, "/todos/{id}", ctx -> {
            var id = ctx.pathParam("id");
            var newContent = ctx.formParam("value");
            var updatedTodo = recordMap.computeIfPresent(id, (_id, oldTodo)
                    -> new Record(id, newContent, oldTodo.completed)
            );
            ctx.html(toHtmx(updatedTodo, false).render());
        });

        //SERVER Handler /todos/{id}/toggle
        server.addHttpHandler(HandlerType.POST, "/todos/{id}/toggle", ctx -> {
            var id = ctx.pathParam("id");

            var updatedTodo = recordMap.computeIfPresent(id, (_id, oldTodo) -> new Record(id, oldTodo.content, !oldTodo.completed));

            ctx.html(toHtmx(updatedTodo, false).render());
        });

        //SERVER Handler /todos/{id}/edit
        server.addHttpHandler(HandlerType.POST, "/todos/{id}/edit", ctx -> {
            var id = ctx.pathParam("id");

            var todo = recordMap.get(id);

            ctx.html(toHtmx(todo, true).render());
        });

        //SERVER Log
        server.after((ctx) -> {
            d.cr("main", ctx.req().getMethod(), ctx.path(), ctx.status());
        });

        server.start();//server.stop();
    }

    private static UlTag toHtmx(Map<String, Record> recordMap) {
        return ul()
                .withId("todo-list")
                .with(
                        recordMap.values().stream()
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

    private static LiTag toHtmx(Record todo, boolean editing) {
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
