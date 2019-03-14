package org.jfrog.eclipse.ui;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Maps;

/**
 * @author yahavi
 */
public class IconManager {
    private static Map<String, Image> icons = Maps.newHashMap();

    public static Image load(String icon) {
    	if (icons.containsKey(icon)) {
    		return icons.get(icon);
    	}
        Bundle bundle = FrameworkUtil.getBundle(IconManager.class);
        URL url = FileLocator.find(bundle, new Path("icons/" + icon + ".png"), null);
        Image image = ImageDescriptor.createFromURL(url).createImage();
        icons.put(icon, image);
        return image;
    }
    
    public static void dispose() {
    	icons.values().forEach(Image::dispose);
    	icons = Maps.newHashMap();
    }
}
