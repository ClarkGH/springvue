package dev.springvue.web;

import dev.springvue.entity.Todo;
import dev.springvue.repository.TodoRepository;
import dev.springvue.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoController(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<TodoDto> list(Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) {
            return List.of();
        }
        return todoRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<TodoDto> create(@RequestBody CreateTodoRequest request, Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null || request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(request.getTitle().trim());
        todo.setCompleted(false);
        todo = todoRepository.save(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(TodoDto.from(todo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoDto> get(@PathVariable Long id, Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return todoRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .map(TodoDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoDto> update(@PathVariable Long id, @RequestBody UpdateTodoRequest request, Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Todo> opt = todoRepository.findById(id).filter(t -> t.getUserId().equals(userId));
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Todo todo = opt.get();
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            todo.setTitle(request.getTitle().trim());
        }
        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
        todo = todoRepository.save(todo);
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Todo> opt = todoRepository.findById(id).filter(t -> t.getUserId().equals(userId));
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        todoRepository.delete(opt.get());
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByUsername(auth.getName())
                .map(u -> u.getId())
                .orElse(null);
    }
}
