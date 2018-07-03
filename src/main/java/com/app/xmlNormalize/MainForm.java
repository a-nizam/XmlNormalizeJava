package com.app.xmlNormalize;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainForm extends JFrame {
    private JTextField textField1;
    private JButton fileButton;
    private JPanel rootPanel;
    private JButton startButton;
    private JLabel messageLabel;

    public MainForm() {
        setContentPane(rootPanel);
        setSize(new Dimension(500, 200));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        fileButton.addActionListener(e -> {
            FileDialog fileDialog = new FileDialog(new Frame(), "Выбор файла", FileDialog.LOAD);
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                textField1.setText(new File(fileDialog.getFile()).getAbsolutePath());
            }
        });

        startButton.addActionListener(e -> {
            XmlNormalize xmlNormalize = new XmlNormalize();
            xmlNormalize.setFilePath(textField1.getText());
            Path newPath = Paths.get(textField1.getText());
            xmlNormalize.setNewFilePath(newPath.getParent().toString() + File.separator + "new.xml");
            try {
                xmlNormalize.normalize();
                messageLabel.setText("Завершено");
            } catch (IOException | XMLStreamException ex) {
                ex.printStackTrace();
            }
        });
    }
}
