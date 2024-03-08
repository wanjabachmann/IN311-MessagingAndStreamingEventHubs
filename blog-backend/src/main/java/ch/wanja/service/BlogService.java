package ch.wanja.service;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import ch.wanja.control.BlogDto;
import ch.wanja.control.BlogRepository;
import ch.wanja.entity.Blog;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

public class BlogService {

    @Inject
    BlogRepository blogRepository;

    @Channel("blogs-in")
    Emitter<ValidationRequest> blogPostEmitter;

    public record ValidationRequest(long id, String content) {
    }

    public record ValidationResponse(long id, boolean valid) {
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.listAll();
    }

    @Transactional
    public void addBlog(BlogDto blogDto) {
        Blog newBlog = new Blog();
        newBlog.setTitle(blogDto.getTitle());
        newBlog.setContent(blogDto.getContent());

        var persistedBlog = persistBlog(newBlog);
        blogRepository.persist(newBlog);

        blogPostEmitter.send(new ValidationRequest(persistedBlog.getId(), persistedBlog.getContent()))
                .toCompletableFuture().join();

        System.out.println(
                "Blog created with id: " + persistedBlog.getId() + " and content: " + persistedBlog.getContent());
    }

    @Transactional
    public Blog persistBlog(Blog newBlog) {
        blogRepository.persist(newBlog);
        return newBlog;
    }

    @Incoming("validation-result")
    @Blocking
    @Transactional
    public void updateValidationStatus(ValidationResponse validationResult) {
        System.out.println("validation-result");

        Blog blog = blogRepository.findById(validationResult.id());

        blog.setValid(validationResult.valid());
        blog.setValidationDate(LocalDate.now());

        System.out.println("Update is: ==> " + validationResult.valid + " for blog with id: " + validationResult.id);

    }
}
