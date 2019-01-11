### ConfigureCore
This module now supports bungeecord!

这个模块现在支持bungeecord了!

Using ConfigureCore in your plugin, you no longer need to care about config.yml or any other .yml file.
You only need to create a configuration class and then let the configuration class use ConfigureCore.
Finally, use `.load(this)` in your plugin.

在你的插件中使用ConfigureCore, 你不再需要关心config.yml或其他任何.yml文件.
你只需要创建一个配置类, 然后让配置类使用ConfigureCore.
最后在你的插件中使用`.load(this)`就可以了

how to use:
1. download the release jar to your project.
2. create the class extend the `org.lintx.plugins.modules.configure.BukkitConfigure` or `org.lintx.plugins.modules.configure.BungeeConfigure`.
3. import `org.lintx.plugins.modules.configure.ConfigureAnnotation` and use the Annotation to serialize your config.
if your use the module, your plugins must rely on this.

don't add the source file to your project.

如何使用:
1. 下载编译包放到你的工程目录并添加到库.
2. 创建配置类并继承`org.lintx.plugins.modules.configure.BukkitConfigure` 或 `org.lintx.plugins.modules.configure.BungeeConfigure`.
3. 引入`org.lintx.plugins.modules.configure.ConfigureAnnotation`并使用注解解析配置文件.
4. 如果你使用了这个库, 你的插件必须依赖这个插件.

不要把源代码直接放进你的工程目录, 以免造成冲突, 除非你修改包名.

[Examples for bukkit](https://github.com/lintx/bukkitapi-configure-example)

[Examples for bungee](https://github.com/lintx/bungeeapi-configure-example)