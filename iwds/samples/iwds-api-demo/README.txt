============================================== I W D S - A P I - D E M O ==============================================

                                    该项目介绍了基于IWDS应用开发所需的一些基本控件的使用方法

1.AmazingViewPager 用来切换多个页面的ViewGroup；
                     支持循环切换；
                     切换方式有“横向”和"竖向"两种。

2.AmazingListView 类似于android.widget.ListView，区别在于AmazingListView每个条目的高度是其可显示区域1/3。
         |
         |--> AmazingFocusListView 在AmazingListView的基础上扩展了滑动结束的定位功能，以此来保证每次滚动结束后每个条目都能显示完全。
                       |
                       |--> AmazingSwipeListView 在AmazingFocusListView的基础上扩展了条目向左滑动删除的功能。

3.RightScrollView 继承自FrameLayout，实现通过手指触控整个布局，将布局向右侧滑动的功能。

4.Dialog

    AmazingDialog 一种全屏显示的Dialog 包含一个文本控件和两个按钮控件；
                  样式有“文字按钮”和“图片按钮”两种。

    AmazingIndeterminateProgressDialog 包含一个自定义的圆形进度条控件和一个文本控件，一般用于正在加载的情况。

5.Progress

    AmazingProgressBar 是一个由8个小球组成的圆形进度条。

    AmazingIndeterminateProgressBar 由5个小球组成的模拟撞球运动的圆形进度条。

    AmazingRingProgressView 一个带百分比的圆形加载进度条。

=======================================================================================================================
