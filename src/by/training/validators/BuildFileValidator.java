package by.training.validators;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildFileValidator extends Task {
    private boolean checkNames = false;
    private List<BuildFile> buildFiles = new ArrayList<>();

    public BuildFileValidator() {
    }

    public BuildFileValidator(boolean checkNames) {
        this.checkNames = checkNames;
    }

    public void setCheckNames(boolean checkNames) {
        this.checkNames = checkNames;
    }

    public BuildFile createBuildFile() {
        BuildFile buildFile = new BuildFile();
        buildFiles.add(buildFile);
        return buildFile;
    }

    @Override
    public void execute() throws BuildException {
        for (BuildFile buildFile : buildFiles) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = null;
            try {
                String fileName = buildFile.getLocation();
                reader = factory.createXMLEventReader(new FileInputStream(fileName));
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    switch (event.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:
                            StartElement startElement = event.asStartElement();
                            Iterator<Attribute> iterator = startElement.getAttributes();
                            while (iterator.hasNext()) {
                                Attribute attribute = iterator.next();
                                String name = attribute.getName().toString();
                                if (XMLConstants.NAME.name().equalsIgnoreCase(name)) {
                                    String value = attribute.getValue();
                                    if (!NameValidator.nameIsValid(value)) {
                                        throw new BuildException("Buildfile " + fileName + " validation failed!\n" +
                                                "name " + value + " is invalid!");
                                    }
                                }
                            }
                        break;
                    }
                }
            } catch (XMLStreamException e) {
                throw new BuildException(e.getMessage());
            } catch (FileNotFoundException e) {
                throw new BuildException(e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        throw new BuildException(e.getMessage());
                    }
                }
            }
        }
    }

    public static class BuildFile {
        private String location;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    private enum XMLConstants {
        NAME
    }

    private static class NameValidator {
        private static final String NAME_PATTERN = "^([a-zA-Z]+[-]??[a-zA-Z]+)$";

        private static boolean nameIsValid(String name) {
            Pattern pattern = Pattern.compile(NAME_PATTERN);
            Matcher matcher = pattern.matcher(name);
            return matcher.matches();
        }
    }
}
