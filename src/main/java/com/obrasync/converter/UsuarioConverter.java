package com.obrasync.converter;

import com.obrasync.model.Usuario;
import com.obrasync.service.UsuarioService;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "usuarioConverter", managed = true)
public class UsuarioConverter implements Converter<Usuario> {
    @Inject private UsuarioService usuarioService;

    @Override
    public Usuario getAsObject(FacesContext context, UIComponent component, String value) {
        return value == null || value.isBlank() ? null : usuarioService.buscarPorId(Long.valueOf(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Usuario usuario) {
        return usuario == null || usuario.getId() == null ? "" : usuario.getId().toString();
    }
}
