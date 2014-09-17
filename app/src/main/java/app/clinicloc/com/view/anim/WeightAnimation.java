package app.clinicloc.com.view.anim;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * Created by syednasharudin on 9/18/2014.
 */
public class WeightAnimation extends Animation {
    private FrameLayout view;
    private float targetWeight;

    public WeightAnimation(FrameLayout view, float targetWeight) {
        this.view = view;
        this.targetWeight = targetWeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        /* Important notes:
        // (1) We take the view's current weight into account for every step of the animation. There
        //     are other ways of doing this but I've found this to be the easiest.
        //
        // (2) Note that we use LinearLayout.LayoutParams here. This is very important - the layout
        //     params used here should be those of the parent of the view you want to animate, not
        //     the view's own params. If you use the wrong ones, it is likely that they won't have a
        //     weight attribute.
        */

        LayoutParams viewParams = (LayoutParams) view.getLayoutParams();
        float currentWeight = viewParams.weight;

        /*
        // Decide which way we need to animate based upon the target weight in relation to the view's
        // current weight.
        */
        if(targetWeight >= currentWeight) {
            viewParams.weight = currentWeight + interpolatedTime * (targetWeight - currentWeight);
        } else {
            viewParams.weight = currentWeight - interpolatedTime * (currentWeight - targetWeight);
        }

        // We only need to re-request a layout. Don't need to bother invalidating.
        view.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
