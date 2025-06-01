图片处理的逻辑是将图片转成自定义的Mat类储存，操作完后转成图片。

下面介绍一下每个类以及里面方法的作用：

### **Mat**：

自定义的数据格式，就是一个三维数组，图片会按照 [ row ][ column ][ channel ]的形式储存，channel依次是Blue，Green，Red
1. 创建一个新的Mat时，需要两个参数 “row“ ”col”
2. 自定义了set和get方法，可以得到或设置mat某一个位置的元素，也能得到mat的row或col
3. print函数能够输出mat所有位置的值


### **ImageOperation**：
该类用于对图像进行操作
1. imageToMat：将图片转成mat，输入图像路径，返回一个mat




### **MatCalculation**：
该类用于对mat进行相关计算

1. computeGradientX，computeGradientY，computeEnergy，computeEnergyMatrix都是与计算mat原有能量值相关
2. 在添加seam中，findNthHorizontalSeam，findNthVerticalSeam分别用于找到横向和竖向能量值最小的seam，输入能量矩阵和一个整数n，返回一个二维数组 [第n条seam][ 第n条seam中储存的路径 ]
3. 在删去seam中，findHorizontalSeam，findVerticalSeam分别用于找到横向和竖向能量值最小的seam，输入能量矩阵，返回一个代表找到的seam的一维数组

### **MatOperation**：
该类用于对mat进行相关操作

1. removeVerticalSeam，removeHorizontalSeam，insertVerticalSeam，insertHorizontalSeam用于删去或添加seam，输入原始mat和seam，返回新的mat
2. matToImage将mat转回image，输入一个mat返回对应的image
