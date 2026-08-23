import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateIcon {
    public static void main(String[] args) {
        try {
            int size = 1024;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            float w = size;
            float h = size;

            // 1. Draw rounded squircle base
            float cornerRadius = w * 0.225f;
            RoundRectangle2D squircle = new RoundRectangle2D.Float(0, 0, w, h, cornerRadius * 2, cornerRadius * 2);
            g.setClip(squircle);

            // Background Multi-point Gradient using overlapping radial and linear paints
            // Base: Deep purple to Royal Blue
            GradientPaint basePaint = new GradientPaint(0, 0, new Color(0x6A, 0x11, 0xD4), 0, h, new Color(0x00, 0x55, 0xF5));
            g.setPaint(basePaint);
            g.fillRect(0, 0, (int)w, (int)h);

            // Top-right Warm Burst (Orange / Magenta / Sunset)
            RadialGradientPaint orangeGlow = new RadialGradientPaint(
                new Point2D.Float(w * 0.88f, h * 0.12f),
                w * 0.85f,
                new float[]{0.0f, 0.35f, 0.70f, 1.0f},
                new Color[]{
                    new Color(255, 122, 0, 255), // Pure Bright Orange
                    new Color(255, 42, 138, 220), // Vivid Hot Pink
                    new Color(144, 19, 254, 120), // Purple
                    new Color(0, 0, 0, 0)
                }
            );
            g.setPaint(orangeGlow);
            g.fillRect(0, 0, (int)w, (int)h);

            // Bottom-right Neon Cyan / Light Blue Glow
            RadialGradientPaint cyanGlow = new RadialGradientPaint(
                new Point2D.Float(w * 0.85f, h * 0.85f),
                w * 0.75f,
                new float[]{0.0f, 0.40f, 0.80f, 1.0f},
                new Color[]{
                    new Color(0, 210, 255, 255), // Electric Cyan
                    new Color(0, 114, 255, 200), // Vibrant Blue
                    new Color(30, 64, 175, 80),
                    new Color(0, 0, 0, 0)
                }
            );
            g.setPaint(cyanGlow);
            g.fillRect(0, 0, (int)w, (int)h);

            // Top-left Violet highlight
            RadialGradientPaint violetGlow = new RadialGradientPaint(
                new Point2D.Float(w * 0.15f, h * 0.15f),
                w * 0.65f,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{
                    new Color(139, 44, 255, 220),
                    new Color(107, 17, 216, 120),
                    new Color(0, 0, 0, 0)
                }
            );
            g.setPaint(violetGlow);
            g.fillRect(0, 0, (int)w, (int)h);

            // Vignette for depth
            RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(w * 0.5f, h * 0.5f),
                w * 0.72f,
                new float[]{0.0f, 0.75f, 1.0f},
                new Color[]{
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 30),
                    new Color(0, 0, 0, 100)
                }
            );
            g.setPaint(vignette);
            g.fillRect(0, 0, (int)w, (int)h);

            // ==========================================
            // 2. INNER GEL CAPSULE (Fluid Multi-color S-core)
            // ==========================================
            float gelStroke = w * 0.225f;

            // Top gel loop (Orange -> Magenta -> Violet)
            Path2D.Float topGel = new Path2D.Float();
            topGel.moveTo(w * 0.38f, h * 0.50f);
            topGel.curveTo(w * 0.38f, h * 0.30f, w * 0.46f, h * 0.27f, w * 0.60f, h * 0.30f);
            topGel.curveTo(w * 0.80f, h * 0.33f, w * 0.81f, h * 0.42f, w * 0.68f, h * 0.43f);

            // Top gel shadow
            g.setStroke(new BasicStroke(gelStroke + 12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0, 0, 0, 40));
            g.draw(topGel);

            // Top gel color
            LinearGradientPaint topGelGrad = new LinearGradientPaint(
                w * 0.38f, h * 0.28f, w * 0.80f, h * 0.42f,
                new float[]{0.0f, 0.45f, 0.80f, 1.0f},
                new Color[]{
                    new Color(255, 140, 0),  // Vivid Orange
                    new Color(255, 42, 138), // Hot Pink
                    new Color(225, 40, 200), // Magenta
                    new Color(155, 30, 240)  // Purple
                }
            );
            g.setPaint(topGelGrad);
            g.setStroke(new BasicStroke(gelStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(topGel);

            // Bottom gel loop (Cyan -> Electric Blue -> Navy)
            Path2D.Float bottomGel = new Path2D.Float();
            bottomGel.moveTo(w * 0.62f, h * 0.50f);
            bottomGel.curveTo(w * 0.62f, h * 0.70f, w * 0.54f, h * 0.73f, w * 0.40f, h * 0.70f);
            bottomGel.curveTo(w * 0.20f, h * 0.67f, w * 0.19f, h * 0.58f, w * 0.32f, h * 0.57f);

            // Bottom gel shadow
            g.setStroke(new BasicStroke(gelStroke + 12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0, 0, 0, 40));
            g.draw(bottomGel);

            // Bottom gel color
            LinearGradientPaint bottomGelGrad = new LinearGradientPaint(
                w * 0.62f, h * 0.72f, w * 0.20f, h * 0.58f,
                new float[]{0.0f, 0.40f, 0.80f, 1.0f},
                new Color[]{
                    new Color(0, 229, 255), // Neon Cyan
                    new Color(0, 140, 255), // Bright Blue
                    new Color(0, 102, 255), // Electric Blue
                    new Color(20, 60, 200)  // Deep Blue
                }
            );
            g.setPaint(bottomGelGrad);
            g.setStroke(new BasicStroke(gelStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(bottomGel);

            // Connecting center flow
            Path2D.Float centerGel = new Path2D.Float();
            centerGel.moveTo(w * 0.40f, h * 0.32f);
            centerGel.curveTo(w * 0.66f, h * 0.44f, w * 0.34f, h * 0.56f, w * 0.60f, h * 0.68f);
            LinearGradientPaint centerGelGrad = new LinearGradientPaint(
                w * 0.40f, h * 0.32f, w * 0.60f, h * 0.68f,
                new float[]{0.0f, 0.35f, 0.65f, 1.0f},
                new Color[]{
                    new Color(255, 110, 0),
                    new Color(255, 30, 120),
                    new Color(120, 40, 240),
                    new Color(0, 210, 255)
                }
            );
            g.setPaint(centerGelGrad);
            g.setStroke(new BasicStroke(gelStroke * 0.95f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(centerGel);

            // ==========================================
            // 3. CRISP WHITE 3D "S" RIBBON
            // ==========================================
            float sStroke = w * 0.185f;

            // Geometry of the white S
            Path2D.Float whiteS = new Path2D.Float();
            whiteS.moveTo(w * 0.77f, h * 0.30f);
            whiteS.curveTo(w * 0.75f, h * 0.11f, w * 0.40f, h * 0.08f, w * 0.25f, h * 0.24f);
            whiteS.curveTo(w * 0.14f, h * 0.38f, w * 0.20f, h * 0.54f, w * 0.38f, h * 0.61f);
            whiteS.curveTo(w * 0.65f, h * 0.70f, w * 0.84f, h * 0.76f, w * 0.70f, h * 0.90f);
            whiteS.curveTo(w * 0.55f, h * 0.98f, w * 0.27f, h * 0.93f, w * 0.23f, h * 0.73f);

            // Multi-pass drop shadows beneath White S for rich realistic depth
            // Ambient soft shadow
            g.setStroke(new BasicStroke(sStroke + 28f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0, 0, 0, 35));
            g.draw(whiteS);

            // Mid shadow
            g.setStroke(new BasicStroke(sStroke + 16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0, 0, 0, 50));
            g.draw(whiteS);

            // Contact shadow
            g.setStroke(new BasicStroke(sStroke + 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(0, 0, 0, 70));
            g.draw(whiteS);

            // Main White S Body with subtle top-to-bottom shading
            LinearGradientPaint whitePaint = new LinearGradientPaint(
                0, h * 0.10f, 0, h * 0.95f,
                new float[]{0.0f, 0.60f, 1.0f},
                new Color[]{
                    new Color(255, 255, 255),
                    new Color(255, 255, 255),
                    new Color(242, 246, 250)
                }
            );
            g.setPaint(whitePaint);
            g.setStroke(new BasicStroke(sStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(whiteS);

            // Translucent lower loop overlay (the blue gel overlapping the lower white tip like in the original art)
            Path2D.Float lowerOverlap = new Path2D.Float();
            lowerOverlap.moveTo(w * 0.32f, h * 0.71f);
            lowerOverlap.curveTo(w * 0.22f, h * 0.68f, w * 0.20f, h * 0.60f, w * 0.29f, h * 0.58f);
            g.setStroke(new BasicStroke(gelStroke * 0.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setPaint(new Color(0, 160, 255, 140));
            g.draw(lowerOverlap);

            // Upper gel overlapping translucency
            Path2D.Float upperOverlap = new Path2D.Float();
            upperOverlap.moveTo(w * 0.68f, h * 0.29f);
            upperOverlap.curveTo(w * 0.78f, h * 0.32f, w * 0.80f, h * 0.40f, w * 0.71f, h * 0.42f);
            g.setStroke(new BasicStroke(gelStroke * 0.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setPaint(new Color(255, 40, 140, 130));
            g.draw(upperOverlap);

            // Top specular gloss spot on white ribbon
            g.setPaint(new RadialGradientPaint(
                new Point2D.Float(w * 0.75f, h * 0.30f),
                w * 0.08f,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(255, 255, 255, 180), new Color(255, 255, 255, 0)}
            ));
            g.fill(new Ellipse2D.Float(w * 0.71f, h * 0.26f, w * 0.08f, w * 0.08f));

            g.dispose();

            // Export to PNG at various standard Android densities
            int[] sizes = {48, 72, 96, 144, 192, 512, 1024};
            String[] dirs = {
                "app/src/main/res/mipmap-mdpi/ic_launcher.png",
                "app/src/main/res/mipmap-hdpi/ic_launcher.png",
                "app/src/main/res/mipmap-xhdpi/ic_launcher.png",
                "app/src/main/res/mipmap-xxhdpi/ic_launcher.png",
                "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
                "app/src/main/res/drawable/ic_splitzy_logo.png",
                "app/src/main/res/drawable/ic_splitzy_hd.png"
            };

            for (int i = 0; i < sizes.length; i++) {
                int s = sizes[i];
                BufferedImage scaled = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(image, 0, 0, s, s, null);
                g2.dispose();

                File targetFile = new File(dirs[i]);
                targetFile.getParentFile().mkdirs();
                ImageIO.write(scaled, "png", targetFile);
                System.out.println("Generated: " + targetFile.getAbsolutePath() + " (" + s + "x" + s + ")");
            }
            System.out.println("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
