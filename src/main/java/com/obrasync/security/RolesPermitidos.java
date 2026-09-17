package com.obrasync.security;

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE, ElementType.METHOD})
public @interface RolesPermitidos { String[] value(); }
