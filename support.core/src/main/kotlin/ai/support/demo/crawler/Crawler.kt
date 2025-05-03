package ai.support.demo.crawler

import com.google.common.io.Resources
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Service
class Crawler(
    private val markdownConverter: HtmlToMarkdownConverter
) {
    private val log = LoggerFactory.getLogger(javaClass);

    @Value("\${crawler.outputDir}")
    lateinit var outputDir: String;
    @Value("\${crawler.inputFile}")
    lateinit var inputFile: String;

    @PostConstruct
    fun handleData() {
        val url: URL? = Resources.getResource(inputFile)
        if (url == null) {
            log.warn("Can't find resource in '{}'", inputFile)
            return
        }
        val text: String? = Resources.toString(url, StandardCharsets.UTF_8)
        if (text == null) {
            log.warn("There isn't text in resource '{}'", inputFile)
            return
        }

        val outDir = getOrCreateOutDirectory()
        text.split("\n").parallelStream()
            .forEach { url ->
            try {
                val html = Jsoup.connect(url).get().html()
                val markdown = markdownConverter.convert(html)
                val fileName = "${StringUtils.substringAfterLast(url, "/")}.md"
                Files.writeString(outDir.resolve(fileName), markdown)
                log.info("File {} saved in {}", url, fileName)
            } catch (e: Exception) {
                log.warn(e.message, e)
            }
        }
    }

    private fun getOrCreateOutDirectory() : Path {
        val outDir = Path.of(outputDir)
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }
        return outDir;
    }
}

@Service
class HtmlToMarkdownConverter {
    fun convert(html: String): String {
        val document = Jsoup.parse(html)
        document.select("script, style, nav, footer").remove()
        return FlexmarkHtmlConverter.builder().build().convert(html);
    }
}