package ai.support.demo.vectorizer.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

@Service
class CrawlerFileScannerService {

    @Value("\${crawler.outputDir}")
    lateinit var outputDir: String;

    //return map filename->file
    fun readDataLake(): Map<String, String> {

        val fileMap : MutableMap<String, String> = ConcurrentHashMap();
        Files.newDirectoryStream(Path.of(outputDir)).use {
            it.forEach { path ->
                if (Files.isReadable(path).not()) {
                    return@forEach
                }
                val fileText = Files.readString(path)
                fileMap.put(path.fileName.toString(), fileText);
            }
        }
        return fileMap;
    }


}