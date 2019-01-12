### ConfigureCore
## about
ConfigureCore: Let your Minecraft plugin load and save the configuration. You no longer need to care about config.yml or any other .yml file.
Support for BukkitAPI and BungeeCord.

## 关于
ConfigureCore：让你的Minecraft插件一句代码加载、保存配置，你不再需要关心config.yml或其他任何.yml文件。
支持BukkitAPI和BungeeCord。

## how to use:
You just need to create a configuration class and let the configuration class annotate with `YamlConfig` (optional: inherit `BukkitConfigure` or `BungeeConfigure`).
Finally, use `Configure.bukkitLoad(plugin,configobj)` or `Configure.bungeeLoad(plugin,configobj)` in your Minecraft plugin (use `configobj.load(plugin)` directly when inheriting).
- If you use this library, your plugin must rely on this plugin.
- Do not put the source code directly into your project directory to avoid conflicts unless you modify the package name (but it is not recommended to modify the package name).
- More usage tips refer to the sample project links at the end of the documentation.

## 如何使用
你只需要创建一个配置类，然后让配置类使用`YamlConfig`注解（可选：继承`BukkitConfigure`或`BungeeConfigure`类）。
最后在你的Minecraft插件中使用`Configure.bukkitLoad(plugin,configobj)`或`Configure.bungeeLoad(plugin,configobj)`(继承时直接使用`configobj.load(plugin)`)就可以了。
- 如果你使用了这个库, 你的插件必须依赖这个插件。
- 不要把源代码直接放进你的工程目录，以免造成冲突，除非你修改包名（但不建议你修改包名）。
- 更多使用方法参考文档末尾的示例工程链接。


[Examples for bukkit](https://github.com/lintx/bukkitapi-configure-example)

[Examples for bungee](https://github.com/lintx/bungeeapi-configure-example)

## Download
[Spigotmc](https://www.spigotmc.org/resources/configurecore.63967/)