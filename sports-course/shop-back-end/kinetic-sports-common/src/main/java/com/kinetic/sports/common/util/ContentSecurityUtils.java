package com.kinetic.sports.common.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ContentSecurityUtils {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif");
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif");
    private static final Pattern HTML_COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DANGEROUS_TAG_PATTERN = Pattern.compile(
            "<\\s*(script|style|iframe|frame|object|embed|link|meta|base|form|input|button|textarea|select)[^>]*>.*?<\\s*/\\s*\\1\\s*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SELF_CLOSING_DANGEROUS_TAG_PATTERN = Pattern.compile(
            "<\\s*(script|style|iframe|frame|object|embed|link|meta|base|input)[^>]*/?>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile("\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN = Pattern.compile(
            "(href|src|xlink:href|formaction|poster)\\s*=\\s*([\"'])\\s*(javascript:|vbscript:|data:text/html)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_EXPRESSION_PATTERN = Pattern.compile(
            "style\\s*=\\s*([\"']).*?(expression\\s*\\(|javascript:|vbscript:|data:text/html).*?\\1",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private ContentSecurityUtils() {
    }

    public static String sanitizeRichText(String html) {
        if (!StringUtils.hasText(html)) {
            return html;
        }
        String sanitized = html.trim();
        sanitized = HTML_COMMENT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = DANGEROUS_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = SELF_CLOSING_DANGEROUS_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = EVENT_HANDLER_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("$1=$2#");
        sanitized = CSS_EXPRESSION_PATTERN.matcher(sanitized).replaceAll("");
        return sanitized;
    }

    public static String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\u0000", "").trim();
    }

    public static String requireSafeExternalUrl(String url) {
        String normalized = normalizeExternalUrl(url);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("外链地址仅支持 http/https");
        }
        return normalized;
    }

    public static String normalizeExternalUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return "";
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
                return "";
            }
            return uri.normalize().toString();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    public static String requireSafeMiniPagePath(String path) {
        String normalized = normalizeMiniPagePath(path);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("页面跳转地址必须以 /pages/ 开头");
        }
        return normalized;
    }

    public static String normalizeMiniPagePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/pages/")) {
            return "";
        }
        if (normalized.contains("..") || normalized.contains("\\") || normalized.contains("\r") || normalized.contains("\n")) {
            return "";
        }
        return normalized;
    }

    public static String validateAndResolveImageExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 10MB");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、gif 图片");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("图片类型不合法");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new IllegalArgumentException("图片内容不合法");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("图片解析失败");
        }
        return extension;
    }

    private static String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}
