# 🐎 Horse Breeding Helper (Fabric)

A Minecraft Fabric mod that helps you identify the best horses for breeding by displaying colored boxes around horses based on their stats, with special highlighting for top performers.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.8-brightgreen)
![Fabric API](https://img.shields.io/badge/Fabric%20API-Required-blue)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-orange)

## ✨ Features

### 🎨 RPG-Style Rarity System
Horses are automatically classified into 5 tiers based on their breeding potential:

- **🟣 Legendary (Magenta)**: 80-100 points - The absolute best horses
- **🔵 Rare (Light Blue)**: 70-79 points - Excellent breeding candidates
- **🟡 Uncommon (Yellow)**: 60-69 points - Good horses worth considering
- **🟢 Common (Green)**: 50-59 points - Average horses
- **🔴 Poor (Red)**: 0-49 points - Lower quality horses

### 🏆 Top Horse Highlighting
- **White pulsing boxes** around the 2 highest-scoring horses in your area
- Larger boxes with gentle pulsing animation to draw attention
- Perfect for quickly identifying the best breeding pairs

### 🎯 Smart Detection
- **100-block radius** detection for comprehensive area coverage
- **Real-time scoring** based on speed, jump height, and health
- **F6 toggle** to enable/disable highlighting with informative chat feedback

## 🧮 Score Calculation

The mod uses a sophisticated scoring system that evaluates three key attributes:

### Speed Score
- Converts internal speed units to blocks per second
- Maximum reference: 14.23 blocks/second
- Formula: `speedScore = min(speedBPS / 14.23, 1.0)`

### Jump Score  
- Converts jump strength to actual jump height in blocks
- Maximum reference: 5.5 blocks
- Uses cubic polynomial conversion for accuracy

### Health Score
- Evaluates horse health relative to possible range
- Range: 15-30 hearts (15 minimum, 30 maximum)
- Formula: `healthScore = (health - 15) / 15`

### Final Score
```
totalScore = (speedScore + jumpScore + healthScore) × 100 ÷ 3
```
*Range: 0-100 points, averaged across all three attributes*

## 🎮 How to Use

1. **Install the mod** in your Fabric mods folder
2. **Press F6** to toggle horse highlighting on/off
3. **Look around** - horses within 100 blocks will show colored boxes
4. **Focus on higher tiers** - prioritize Legendary and Rare horses for breeding
5. **Watch for white boxes** - these mark the top 2 horses in your area

## 📦 Installation

### Requirements
- **Minecraft 1.21.8**
- **Fabric Loader**
- **Fabric API**

### Steps
1. Download and install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest Horse Breeding Helper release
4. Place both `.jar` files in your `mods` folder
5. Launch Minecraft with the Fabric profile

## 🎯 Perfect For

- **Horse enthusiasts** who want to optimize their stable
- **Speed builders** looking for the fastest horses
- **Jump course designers** needing high-jumping horses
- **Multiplayer servers** where horse trading is important
- **Anyone** who wants to breed superior horses efficiently

## 🔧 Technical Details

- **Client-side mod** - works on any server
- **WorldRenderEvents integration** for smooth rendering
- **Multi-layered fallback system** for maximum compatibility
- **Real-time calculation** with efficient caching
- **No performance impact** on server or other players

## 🐛 Reporting Issues

Found a bug or have a suggestion? Please open an issue on GitHub with:
- Your Minecraft version
- Fabric Loader version
- Fabric API version
- Steps to reproduce the issue
- Screenshots if applicable

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with [Fabric](https://fabricmc.net/)
- Thanks to the Minecraft modding community
- Inspired by the need for better horse breeding tools

---

**Happy Horse Breeding!** 🐎✨
