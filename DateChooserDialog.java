import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DateChooserDialog extends JDialog implements AppConstants {
    private JSpinner dateSpinner;
    private JTextField targetField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DateChooserDialog(JFrame parent, JTextField targetField) {
        super(parent, "Select Date", true); 
        this.targetField = targetField;
        getContentPane().setBackground(BACKGROUND_COLOR);

        setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        
        Date initialDate = new Date();
        try {
            if (!targetField.getText().isEmpty() && !targetField.getText().equals("N/A")) {
                initialDate = dateFormat.parse(targetField.getText());
            }
        } catch (Exception e) {
            // Use current date if parsing fails
        }
        
        SpinnerDateModel model = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_YEAR);
        dateSpinner = new JSpinner(model);
        
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setFont(BODY_FONT);

        JButton selectButton = new JButton("Select");
        selectButton.setBackground(PRIMARY_COLOR);
        selectButton.setForeground(Color.WHITE);
        selectButton.addActionListener(e -> selectDate());

        add(dateSpinner);
        add(selectButton);

        pack();
        setLocationRelativeTo(parent);
    }

    private void selectDate() {
        Date selectedDate = (Date) dateSpinner.getValue();
        targetField.setText(dateFormat.format(selectedDate));
        this.dispose();
    }
}