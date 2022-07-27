package com.example.neteasecloudmusictext.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutWidget extends ViewGroup {
    public FlowLayoutWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //子View的横向间隔、纵向间隔

    private final int horizontalSpace = 30;
    private final int verticalSpace = 20;

    //保存测量的子View， 每一个元素为一行的子View数组

    private final List<List<View>> allLines = new ArrayList<>();

    //记录每一行的最大高度，用于布局

    private final List<Integer> heights = new ArrayList<>();


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        allLines.clear();
        heights.clear();

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int usedWidth = 0;
        int height = 0;

        //父布局对FlowLayout的约束宽高(还剩多少可以使用）
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft -
                paddingRight;
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop -
                paddingBottom;

        //FlowLayout的测量宽高
        int needHeight = 0;
        int needWidth = 0;

        //每一行存储view
        @SuppressLint("DrawAllocation") List<View> line = new ArrayList<>();

        int count = getChildCount();

        //遍历每一个view，并把进行测量
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                    paddingLeft + paddingRight, child.getLayoutParams().width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
                    paddingTop + paddingBottom, child.getLayoutParams().height);
            //调用子view的measure方法进行测量（它还会在内部调用它的onMeasure方法
            child.measure(childWidthSpec, childHeightSpec);


            // 进行判断是否换行
            if (usedWidth + horizontalSpace + child.getMeasuredWidth() > selfWidth) {
                //当前行无法在放下下一个view，则保存当前行的Views集合以及当前行的最大高度，
                heights.add(height + verticalSpace);
                allLines.add(line);
                //所有行的最大宽度
                needWidth = Math.max(needWidth, usedWidth);
                //所有行的高度之和
                needHeight += height + verticalSpace;

                //重置下一行的使用宽度、高度、View集合，便于下一次遍历
                usedWidth = 0;
                height = 0;
                line = new ArrayList<>();
            }
            //获取当前行的最大高度，作为当前行的高度
            height = Math.max(height, child.getMeasuredHeight());
            //记录已经使用的宽度（第一个元素不需要加横向间隔）
            usedWidth += child.getMeasuredWidth() + (line.size() == 0 ? 0 :
                    horizontalSpace);
            //保存已经测量及模拟布局的View
            line.add(child);

            //记录最后一行的数据，这是因为这里判断的是换行才记录,很多情况是不满足换行要求的
            if (i == count - 1) {
                heights.add(height + verticalSpace);
                allLines.add(line);
                needWidth = Math.max(needWidth, usedWidth);
                needHeight += height + verticalSpace;
            }
        }

        // 这里要完成对自己的的测量
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 看自己怎么设置，看这个是wrap还是定了高度
        //如果mode为MeasureSpec.EXACTLY， 则使用widthMeasureSpec中的size
        //不然使用测量得到的size， 宽高同理
        int realWidth = widthMode == MeasureSpec.EXACTLY ? selfWidth : needWidth;
        int realHeight = heightMode == MeasureSpec.EXACTLY ? selfHeight : needHeight;

        //保存测量的宽和高
        setMeasuredDimension(realWidth + paddingLeft + paddingRight,
                //如果只有一行，不需要纵向间隔
                realHeight + paddingTop + paddingBottom - (allLines.size() > 0 ?
                        verticalSpace : 0));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < allLines.size(); i++) {
            List<View> line = allLines.get(i);
            for (int j = 0; j < line.size(); j++) {
                View child = line.get(j);
                child.layout(left, top, left + child.getMeasuredWidth(),
                        top + child.getMeasuredHeight());
                //一行中View布局后每次向后移动child的测量宽 + 横向间隔
                left += child.getMeasuredWidth() + horizontalSpace;
            }
            //每一行布局从paddingLeft开始
            left = getPaddingLeft();
            //布局完成一行，向下移动当前行的最大高度
            top += heights.get(i);
        }
    }
}
