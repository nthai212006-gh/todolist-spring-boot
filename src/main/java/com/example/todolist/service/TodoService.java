package com.example.todolist.service;

import com.example.todolist.dto.TodoRequest;
import com.example.todolist.exception.TodoNotFoundException;
import com.example.todolist.model.Todo;
import com.example.todolist.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    public Todo create(TodoRequest req) {
        Todo todo = new Todo(
                req.getTitle(),
                req.getDescription(),
                req.getDueDate(),
                req.isCompleted()
        );
        return todoRepository.save(todo);
    }

    public Todo update(Long id, TodoRequest req) {
        Todo todo = findById(id);
        todo.setTitle(req.getTitle());
        todo.setDescription(req.getDescription());
        todo.setDueDate(req.getDueDate());
        todo.setCompleted(req.isCompleted());
        return todoRepository.save(todo);
    }

    public void deleteById(Long id) {
        Todo todo = findById(id);
        todoRepository.delete(todo);
    }
}
