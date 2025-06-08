package ru.netology.repository;

import ru.netology.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Stub
public class PostRepository {
  private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
  private final AtomicLong counter = new AtomicLong(0);

  public List<Post> all() {
    return List.copyOf(posts.values());
  }

  public Optional<Post> getById(long id) {
      return Optional.ofNullable(posts.get(id));
  }

  public Post save(Post post) {
    long curVal, newVal;

    do {
      curVal = counter.get();
      newVal = (curVal + 1) % Long.MAX_VALUE;
    } while (!counter.compareAndSet(curVal, newVal));

    posts.put(newVal, post);

    return post;
  }

  public void removeById(long id) {
    posts.remove(id);
  }
}
