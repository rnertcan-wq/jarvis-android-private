package local.mertcan.jarvis;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/** Lightweight animated JARVIS orb for the main dashboard. */
final class JarvisOrbView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ValueAnimator pulse;
    private float phase;

    JarvisOrbView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        pulse = ValueAnimator.ofFloat(0f, 1f);
        pulse.setDuration(2600L);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.addUpdateListener(animator -> {
            phase = (float) animator.getAnimatedValue();
            invalidate();
        });
        pulse.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) * 0.32f;
        float breath = 0.96f + (phase * 0.06f);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                cx, cy, radius * 1.8f,
                new int[]{0x7745cfff, 0x2245cfff, 0x0045cfff},
                null, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, radius * 1.8f, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(0x6645cfff);
        canvas.drawCircle(cx, cy, radius * (1.12f + 0.08f * phase), paint);
        paint.setStrokeWidth(dp(0.8f));
        paint.setColor(0x337de0ff);
        canvas.drawCircle(cx, cy, radius * 1.28f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                cx - radius * 0.28f, cy - radius * 0.34f, radius * 1.45f,
                new int[]{0xffe2f9ff, 0xff72d9ff, 0xff197fbe, 0xff071d34},
                new float[]{0f, 0.22f, 0.62f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, radius * breath, paint);
        paint.setShader(null);

        paint.setColor(0x66ffffff);
        canvas.drawOval(
                cx - radius * 0.48f, cy - radius * 0.62f,
                cx + radius * 0.06f, cy - radius * 0.20f, paint);
        paint.setColor(0x7745cfff);
        canvas.drawCircle(cx + radius * 0.45f, cy + radius * 0.45f, radius * 0.07f, paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDetachedFromWindow() {
        pulse.cancel();
        super.onDetachedFromWindow();
    }
}
