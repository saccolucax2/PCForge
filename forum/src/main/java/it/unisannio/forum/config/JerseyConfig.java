package it.unisannio.forum.config;

import it.unisannio.forum.presentation.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(CommentController.class);
        register(PostController.class);
        register(LikeController.class);
    }
}