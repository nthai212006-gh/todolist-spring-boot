package com.example.todolist.controller;

import com.example.todolist.dto.TodoRequest;
import com.example.todolist.model.Todo;
import com.example.todolist.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("todos", todoService.findAll());
        return "todos/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("todoRequest", new TodoRequest());
        return "todos/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("todoRequest") TodoRequest request,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "todos/form";
        }
        todoService.create(request);
        return "redirect:/todos";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Todo todo = todoService.findById(id);
        TodoRequest req = new TodoRequest(
                todo.getTitle(),
                todo.getDescription(),
                todo.getDueDate(),
                todo.isCompleted()
        );
        model.addAttribute("id", id);
        model.addAttribute("todoRequest", req);
        return "todos/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("todoRequest") TodoRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("id", id);
            return "todos/form";
        }
        todoService.update(id, request);
        return "redirect:/todos";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        todoService.deleteById(id);
        return "redirect:/todos";
    }
}
