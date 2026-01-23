package it.unisannio.gateway.security;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.*;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HasRole {
    String[] value(); // Array di ruoli ammessi

}