package ru.netology.servlet;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.netology.controller.PostController;
import ru.netology.exception.NotFoundException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MainServlet extends HttpServlet {
  private final Map<String, BiConsumer<HttpServletRequest, HttpServletResponse>> routes = new HashMap<>();
  private PostController controller;

  private String getRouteKey(HttpServletRequest req) {
    final var path = req.getRequestURI();
    final var rootPath = path.startsWith("/api/posts/") ?
            "/api/posts" :
            path;
    return req.getMethod() + ":" + rootPath;
  }

  private void addHandler(String method,
                          String path,
                          BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
    final var routeKey = method + ":" + path;
    routes.put(routeKey, handler);
  }

  @Override
  public void init() {
    final var context = new AnnotationConfigApplicationContext("ru.netology");
    controller = context.getBean(PostController.class);

    addHandler("GET",
            "/api/posts",
            (req, resp) -> {
              final var path = req.getRequestURI();
              if (path.matches("/api/posts/\\d+")) {
                final var id = Long.parseLong(path.substring(path.lastIndexOf("/")+1));
                  try {
                      controller.getById(id, resp);
                  } catch (IOException e) {
                      throw new RuntimeException(e);
                  }
              } else {
                try {
                  controller.all(resp);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              }
            });

    addHandler("POST",
            "/api/posts",
            (req, resp) -> {
                try {
                    controller.save(req.getReader(), resp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

    addHandler("DELETE",
            "/api/posts",
            (req, resp) -> {
              final var path = req.getRequestURI();
              if (path.matches("/api/posts/\\d+")) {
                final var id = Long.parseLong(path.substring(path.lastIndexOf("/")+1));
                controller.removeById(id, resp);
              } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              }
            });
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) {
    // если деплоились в root context, то достаточно этого
    try {
      final var routeKey = getRouteKey(req);
      BiConsumer<HttpServletRequest, HttpServletResponse> handler = routes.get(routeKey);
      if (handler != null) {
        handler.accept(req, resp);
      } else {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
    catch (NotFoundException e) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}

