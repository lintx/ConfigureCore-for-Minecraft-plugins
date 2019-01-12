package org.lintx.plugins.modules.configure;

import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Configure {
    public static void bungeeLoad(Plugin plugin,Object object){
        bungeeLoad(plugin,object,null);
    }

    public static void bungeeLoad(Plugin plugin,Object object,String ymlPath){
        new BungeeConfigure().load(plugin,object,ymlPath);
    }

    public static void bukkitLoad(JavaPlugin plugin,Object object){
        bukkitLoad(plugin,object,null);
    }

    public static void bukkitLoad(JavaPlugin plugin,Object object,String ymlPath){
        new BukkitConfigure().load(plugin,object,ymlPath);
    }

    public static void bungeeSave(Plugin plugin,Object object){
        bungeeSave(plugin,object,null);
    }

    public static void bungeeSave(Plugin plugin,Object object,String ymlPath){
        new BungeeConfigure().save(plugin,object,ymlPath);
    }

    public static void bukkitSave(JavaPlugin plugin,Object object){
        bukkitSave(plugin,object,null);
    }

    public static void bukkitSave(JavaPlugin plugin,Object object,String ymlPath){
        new BukkitConfigure().save(plugin,object,ymlPath);
    }



    protected String pathWithField(Field field){
        String path = null;
        try {
            if (!field.isAnnotationPresent(YamlConfig.class)){
                return null;
            }
            YamlConfig anno = field.getAnnotation(YamlConfig.class);
            if (anno==null){
                return null;
            }
            field.setAccessible(true);
            if (anno.path().equals("")){
                path = field.getName();
            }
            else {
                path = anno.path();
            }
        }
        catch (Exception e){

        }
        return path;
    }

    protected boolean clzHasAnnotation(Class clz){
        boolean resutlt = false;
        for (Field field : clz.getDeclaredFields()){
            if (field.isAnnotationPresent(YamlConfig.class)){
                resutlt = true;
                break;
            }
        }
        return resutlt;
    }

    protected String pathWithClass(Class<?> clz){
        YamlConfig anno = clz.getAnnotation(YamlConfig.class);
        String path = null;
        if (anno!=null){
            if (anno.path().equals("")){
                path = "config.yml";
            }
            else {
                path = anno.path();
            }
        }
        return path;
    }
}
