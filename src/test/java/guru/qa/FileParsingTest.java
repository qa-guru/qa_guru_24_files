package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import guru.qa.model.Human;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class FileParsingTest {

  private final ClassLoader cl = FileParsingTest.class.getClassLoader();
  private final Gson gson = new Gson();

  @Test
  void pdfParsingTest() throws Exception {
    open("https://junit.org/junit5/docs/current/user-guide/");
    File pdfFile = $("a[href='junit-user-guide-5.10.1.pdf']").download();
    PDF pdf = new PDF(pdfFile);
    Assertions.assertEquals("JUnit 5 User Guide", pdf.title);
  }

  @Test
  void xlsParsingTest() throws Exception {
    open("https://excelvba.ru/programmes/Teachers?ysclid=lfcu77j9j9951587711");
    File xlsFile = $("a[href='https://ExcelVBA.ru/sites/default/files/teachers.xls']").download();
    XLS xls = new XLS(xlsFile);
    Assertions.assertEquals(
        "Белый Владимир Михайлович",
        xls.excel.getSheet("База данных")
            .getRow(4)
            .getCell(1)
            .getStringCellValue()
    );
  }

  @Test
  void csvParsingTest() throws Exception {
    try (InputStream is = cl.getResourceAsStream("files/qa_guru.csv");
         CSVReader csvReader = new CSVReader(new InputStreamReader(is))) {
      List<String[]> content = csvReader.readAll();
      Assertions.assertArrayEquals(
          new String[]{"Teacher", "lesson"}, content.get(0)
      );
      Assertions.assertArrayEquals(
          new String[]{"Tuchs", "Files"}, content.get(1)
      );
    }
  }

  @Test
  void zipParsingTest() throws Exception {
    try (InputStream is = cl.getResourceAsStream("files/sample.zip");
         ZipInputStream zis = new ZipInputStream(is)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        System.out.println(entry.getName());
      }
    }
  }

  @Test
  void jsonParsingTest() throws Exception {
    try (InputStream is = cl.getResourceAsStream("files/human.json");
         Reader reader = new InputStreamReader(is)) {
      JsonObject object = gson.fromJson(reader, JsonObject.class);

      Assertions.assertEquals("Dima", object.get("name").getAsString());
      Assertions.assertEquals(34, object.get("age").getAsInt());
      Assertions.assertArrayEquals(
          new String[]{"speaking", "teaching"},
          object.get("hobby").getAsJsonArray()
              .asList()
              .stream()
              .map(JsonElement::getAsString)
              .toArray()
      );
      JsonObject passport = object.get("passport").getAsJsonObject();
      Assertions.assertEquals("МВД", passport.get("issuer").getAsString());
    }
  }

  @Test
  void jsonParsingTestNextLevel() throws Exception {
    try (InputStream is = cl.getResourceAsStream("files/human.json");
         Reader reader = new InputStreamReader(is)) {
      Human object = gson.fromJson(reader, Human.class);

      Assertions.assertEquals("Dima", object.name);
      Assertions.assertEquals(34, object.age);
      Assertions.assertArrayEquals(
          new String[]{"speaking", "teaching"},
          object.hobby.toArray()
      );
      Assertions.assertEquals("МВД", object.passport.issuer);
    }
  }
}
