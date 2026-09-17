package com.obrasync.converter;

import com.obrasync.model.Obra;
import com.obrasync.service.ObraService;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "obraConverter", managed = true)
public class ObraConverter implements Converter<Obra> {
    @Inject private ObraService obraService;

    @Override
    public Obra getAsObject(FacesContext context, UIComponent component, String value) {
        return value == null || value.isBlank() ? null : obraService.buscarPorId(Long.valueOf(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Obra obra) {
        return obra == null || obra.getId() == null ? "" : obra.getId().toString();
    }
}
