# 🐎 Horse Breeding Helper (Fabric)

A Minecraft Fabric mod that helps you identify the best horses for breeding by displaying their scores and diamond values when mounting them, with RPG-style rarity classification and optional visual highlighting.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.8-brightgreen)
![Fabric API](https://img.shields.io/badge/Fabric%20API-Required-blue)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-orange)
![Version](https://img.shields.io/badge/Version-1.0.1-yellow)

## ✨ Features

### �️ **Horse Score Display on Mount**
When you mount any horse, instantly see:
- **Score**: Calculated from speed, jump height, and health (0-100 scale)
- **Tier**: RPG-style classification (Legendary, Rare, Uncommon, Common, Poor)
- **Diamond Value**: Exponential pricing system showing horse worth in diamonds

**Display Example:**
```
Horse Score: 76.3 (Rare) | Value: 5.41 💎
```

### 💎 **Diamond Pricing System**
Horses are valued using an exponential curve that rewards excellence:
- **Score 50-60**: 0.00-0.13 💎 (Affordable starter horses)
- **Score 70-80**: 0.76-2.90 💎 (Quality investment horses)
- **Score 90-100**: 10.56-100.00 💎 (Elite legendary horses)

Formula: `value = 100 × ((score - 50) / 50)^3.5`

### 🎨 **RPG-Style Rarity System** *(Optional F6 Toggle)*
Enable visual highlighting to see horses with colored boxes:
- **🟣 Legendary (Magenta)**: 80-100 points - The absolute best horses
- **🔵 Rare (Light Blue)**: 70-79 points - Excellent breeding candidates
- **🟡 Uncommon (Yellow)**: 60-69 points - Good horses worth considering
- **🟢 Common (Green)**: 50-59 points - Average horses
- **🔴 Poor (Red)**: 0-49 points - Lower quality horses

### 🏆 **Top Horse Highlighting** *(F6 Mode)*
- **White pulsing boxes** around the 2 highest-scoring horses in your area
- **100-block radius** detection for comprehensive area coverage
- **Debug mode** shows detailed stat breakdown when F6 is enabled

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

### **Basic Usage (Always Active)**
1. **Mount any horse** → Instantly see its score, tier, and diamond value in the action bar
2. **Compare horses** → Mount different horses to see which are worth breeding
3. **Make informed decisions** → Use the diamond values to assess trading worth

### **Advanced Analysis (F6 Toggle)**
1. **Press F6** → Enable visual highlighting and debug mode
2. **Look around** → Horses within 100 blocks show colored rarity boxes
3. **White pulsing boxes** → Identify the top 2 horses in your area
4. **Mount horses** → Get detailed debug breakdown of individual stats
5. **Press F6 again** → Disable highlighting to reduce visual clutter

### **Interpreting the Display**
- **Score**: Higher is better (0-100 scale)
- **Tier Colors**: Purple = best, Red = worst
- **Diamond Value**: Exponential - small score improvements = big value increases at high levels

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

- **Horse breeders** who want to optimize their breeding programs
- **Economy servers** where horse trading is important
- **Speed enthusiasts** looking for the fastest horses
- **Jump course designers** needing high-jumping horses  
- **Casual players** who want to know if their horse is worth keeping
- **Multiplayer traders** who need to assess horse values quickly

## 🔧 Technical Details

- **Client-side mod** - works on any server, no server-side installation needed
- **Accurate scoring** - uses official Minecraft horse stat ranges
- **Efficient calculation** - real-time scoring with optimized algorithms
- **Clean UI** - essential info always available, detailed info on demand
- **No performance impact** - lightweight design, no lag for other players

### **Scoring Algorithm**
Uses official Minecraft horse attribute ranges:
- **Speed**: 0.1125-0.3375 internal units
- **Jump**: 0.4-1.0 internal strength units  
- **Health**: 15-30 hearts

Each attribute is normalized to 0-1, then averaged and scaled to 0-100 points.

## � Version History

### v1.0.1 (Latest - Hotfix)
- ✅ Added horse score display when mounting horses
- ✅ Implemented diamond pricing system with exponential curve
- ✅ Fixed scoring calculations using official Minecraft ranges
- ✅ Added conditional debug mode (F6 toggle)
- ✅ Improved user experience with cleaner information display

### v1.0.0 (Initial Release)
- ✅ RPG-style horse classification system
- ✅ Visual highlighting with colored boxes
- ✅ Top-2 horse identification
- ✅ F6 toggle for enabling/disabling features

## �🐛 Reporting Issues

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
