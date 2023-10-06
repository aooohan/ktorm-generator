<div align="left" >
    <img src="./src/main/resources/META-INF/pluginIcon.svg" alt="Editor" width="100">
    <h1>ktorm-generator</h1>
</div>



![Build](https://github.com/aooohan/ktorm-generator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/22855-ktormgenerator.svg)](https://plugins.jetbrains.com/plugin/22855-ktormgenerator)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/22855-ktormgenerator.svg)](https://plugins.jetbrains.com/plugin/22855-ktormgenerator)


<!-- Plugin description -->
KtormGenerator is a plugin for generating [Ktorm](https://github.com/kotlin-orm/ktorm) entity class from database table.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "ktorm-generator"</kbd> >
  <kbd>Install</kbd>
  
- Manually:

  Download the [latest release](https://github.com/aooohan/ktorm-generator/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## How to use?
1. Select some tables from Database Tools View, then pop-up menu bar, finally click Ktorm-Generator option.

<img src="./doc/p1.png" alt="Editor" >

2. Configure the information needed to generate.(you can also modify the final class name) and click Ok button

<img src="./doc/p2.png" alt="Editor" >

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
