/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 76905
 */

import com.kitfox.svg.app.beans.SVGIcon;
import java.awt.Dimension;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SvgIcon {
    // Carga un SVG desde recursos del proyecto y lo devuelve como ImageIcon
    public static final int SMALL  = 16;
    public static final int MEDIUM = 24;
    public static final int LARGE  = 32;

    private static final Map<String, SVGIcon> cache = new HashMap<>();

    private SvgIcon() {}

    // Carga un SVG como ícono con tamaño personalizado.

    public static SVGIcon load(String resourcePath, int size) {
        String key = resourcePath + "_" + size;

        if (cache.containsKey(key)) return cache.get(key);

        try {
            URL url = SvgIcon.class.getResource(resourcePath);

            if (url == null) {
                System.err.println("SvgIcon: No se encontró → " + resourcePath);
                return null;
            }

            SVGIcon icon = new SVGIcon();
            icon.setSvgURI(url.toURI());
            icon.setScaleToFit(true);
            icon.setPreferredSize(new Dimension(size, size));
            icon.setAntiAlias(true);

            cache.put(key, icon);
            return icon;

        } catch (Exception e) {
            System.err.println("SvgIcon: Error al cargar → " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    //Versión corta — usa 16px por defecto, ideal para menús.
    public static SVGIcon load(String resourcePath) {
        return load(resourcePath, SMALL);
    }

    //Limpia el cache si se necesita liberar memoria.
    public static void clearCache() {
        cache.clear();
    }
}
