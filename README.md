# OpenComet v2.0

OpenComet is a powerful Fiji plugin for automated single cell electrophoresis (comet assay) image analysis. This tool helps researchers analyze DNA damage in cells through the comet assay technique by automatically detecting and measuring comets in microscopy images.

## Overview

OpenComet provides an automated solution for:
- Detecting comets in microscopy images
- Analyzing comet head and tail parameters
- Measuring DNA damage through various metrics
- Generating comprehensive analysis reports

## Features

- **Automated Comet Detection**: Automatically identifies and analyzes comets in microscopy images
- **Multiple Analysis Methods**:
  - Background correction
  - Multiple head finding algorithms (Auto, Profile, Brightest)
  - Various thresholding methods which are user selectable
- **Interactive GUI**: User-friendly interface for:
  - Input file selection
  - Output directory configuration
  - Analysis parameter adjustment
  - Real-time visualization
- **Comprehensive Measurements**:
  - Head and tail parameters
  - DNA damage metrics
- **Visualization Tools**:
  - Color-coded comet display
  - Profile plots
  - Interactive comet selection

## Installation

1. Ensure you have ImageJ or Fiji installed on your system
2. Download the latest release from the dist directory
3. Place the plugin JAR file in your ImageJ plugins directory
4. Restart ImageJ

## Usage

1. Launch ImageJ
2. Go to Plugins > OpenComet
3. Configure analysis parameters:
   - Select input files
   - Choose output directory
   - Choos output file name
   - Adjust analysis settings
5. Click "Run" to start the analysis
6. Following analysis, use the interactive output image to select or deselct comets
7. Click update to export updated results

## Analysis Parameters

- **Background Correction**: Enable/disable background correction
- **Head Finding Method**:
  - Auto: Automatic head detection
  - Profile: Based on intensity profile
  - Brightest: Based on brightest point
- **Threshold Method**: Various options for detecting comet sizes. Triangle and Huang typically function the best.

## Output

The plugin generates:
- Detailed measurements for each comet
- Statistical summary
- Visual overlays of detected comets
- CSV export of results

## Citation

If you use this plugin in your research, please cite:
```
Gyori BM, Venkatachalam G, et al. OpenComet: An automated tool for comet assay image analysis
```

## License

This plugin is released under the GNU General Public License version 3. See the LICENSE file for details.

## Authors

- Original Author: Benjamin Gyori (National University of Singapore)
- Updated by: Josiah Murray (Medical College of Wisconsin)

## Support

For issues, feature requests, or contributions, please visit the GitHub repository.

## Acknowledgments

Thanks to Benjamin Gyori for writing much of the base code used in this new version. 