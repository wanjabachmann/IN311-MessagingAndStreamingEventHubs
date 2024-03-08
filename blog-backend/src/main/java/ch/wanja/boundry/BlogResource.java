package ch.wanja.boundry;

import ch.wanja.control.BlogDto;
import ch.wanja.entity.Blog;
import ch.wanja.service.BlogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/blogs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BlogResource {
    @Inject
    BlogService blogService;

    @GET
    public List<Blog> getAllBlogs() {
        return blogService.getAllBlogs();
    }

    @POST
    public void createBlog(BlogDto blogDto) {
        blogService.addBlog(blogDto);
    }

}