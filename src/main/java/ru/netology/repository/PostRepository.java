package ru.netology.repository;

import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PostRepository {
  private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
  private final AtomicLong counter = new AtomicLong(0);

  public List<Post> all() {
    return List.copyOf(posts.values());
  }

  public Optional<Post> getById(long id) {
    if (!posts.containsKey(id)) {
      throw new NotFoundException(String.format("Id %d not found", id));
    }
    return Optional.of(posts.get(id));
  }

  public Post save(Post post) {
    if (post.getId() == 0) {
      long curVal, newVal;

      do {
        curVal = counter.get();
        newVal = (curVal + 1) % Long.MAX_VALUE;
      } while (!counter.compareAndSet(curVal, newVal));

      post.setId(newVal);
      posts.put(newVal, post);
    } else {
      if (!posts.containsKey(post.getId())) {
        throw new NotFoundException(String.format("Id %d not found", post.getId()));
      } else {
        posts.put(post.getId(), post);
      }
    }

    return post;
  }

  public void removeById(long id) {
    if (!posts.containsKey(id)) {
      throw new NotFoundException(String.format("Id %d not found", id));
    }
    posts.remove(id);
  }
}
