package ai.support.demo.crawler

import com.google.common.io.Resources
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class Crawler(
    private val markdownConverter: HtmlToMarkdownConverter,
) {
    private val log = LoggerFactory.getLogger(javaClass);

    fun handleDataFromFile(linksFilePath: String, outputDir: String) {
        val url: URL? = Resources.getResource(linksFilePath)
        if (url == null) {
            log.warn("Can't find resource in '{}'", linksFilePath)
            return
        }
        val text: String? = Resources.toString(url, StandardCharsets.UTF_8)
        if (text == null) {
            log.warn("There isn't text in resource '{}'", linksFilePath)
            return
        }

        val outDir = getOrCreateOutDirectory(outputDir)

        text.split("\n").parallelStream()
            .forEach { url -> handleData(url, outDir) }
    }

    fun handleData(url: String, outputDir: Path) {
        try {
            val html = Jsoup.connect(url).get().html()
            val markdown = markdownConverter.convert(html)
            val fileName = "${StringUtils.substringAfterLast(url, "/")}.md"
            Files.writeString(outputDir.resolve(fileName), markdown)
            log.info("File {} saved in {}", url, fileName)
        } catch (e: Exception) {
            log.warn(e.message, e)
        }
    }

    fun getOrCreateOutDirectory(outputDir: String): Path {
        val outDir = Path.of(outputDir)
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }
        return outDir;
    }
}

class HtmlToMarkdownConverter {
    fun convert(html: String): String {
        val document = Jsoup.parse(html)
        document.select("script, style, nav, footer, header").remove()
        return FlexmarkHtmlConverter.builder()
            .build()
            .convert(document.toString())
            .trimIndent();
    }
}

fun main() {
    val crawler = Crawler(
        HtmlToMarkdownConverter(),
    )

    val outDir = crawler.getOrCreateOutDirectory("./data-lake")
    crawler.handleData(
        "https://example.com",
        outDir
    );
}