package com.arm.toolantik;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.os.Build;
import android.widget.*;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("Arm-antik");
    }

    public native String[] convertAsmToHex(String asmCode);
    public native String[] convertHexToArmFormats(String hexString);

    private EditText hexCodeInput;
    private int[] boxIds = {R.id.box1, R.id.box2, R.id.box3, R.id.box4, R.id.box5};
    private int[] indexMapping = {1, 0, 4, 2, 3};

    private String[] titles = {
        "ARM64",
        "ARM",
        "ARM Big Endian",
        "THUMB",
        "THUMB Big Endian"
    };

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		

        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        hexCodeInput = findViewById(R.id.hexCodeInput);
        final Spinner commandSpinner = findViewById(R.id.commandSpinner);
        ImageButton btnCopy = findViewById(R.id.btnCopy);
        ImageButton btnClipboard = findViewById(R.id.btnClipboard);
        ImageButton btnClose = findViewById(R.id.btnClose);

        setBoxTitles();

        loadPreferences(commandSpinner);
        hexCodeInput.addTextChangedListener(new android.text.TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
					String inputText = hexCodeInput.getText().toString().trim();
					if (isHexFormat(inputText)) {
						commandSpinner.setSelection(0);
					} else {
						commandSpinner.setSelection(1);
					}
				}

				@Override
				public void afterTextChanged(android.text.Editable editable) {
				}
			});
        btnCopy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String textToCopy = hexCodeInput.getText().toString();
					copyToClipboard(textToCopy);
				}
			});

        btnClipboard.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					pasteFromClipboard();
				}
			});

        btnClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					hexCodeInput.setText("");
				}
			});

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					updateCodeSnippets(commandSpinner);
				}
			});

        commandSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(android.widget.AdapterView<?> parentView, View selectedItemView, int position, long id) {
					String selectedCommand = commandSpinner.getSelectedItem().toString();
					saveSelectedCommand(selectedCommand);
				}

				@Override
				public void onNothingSelected(android.widget.AdapterView<?> parentView) {
				}
			});
	}

    private void setBoxTitles() {
		for (int i = 0; i < boxIds.length; i++) {
			View box = findViewById(boxIds[i]);
			TextView title = box.findViewById(R.id.titleText);
			title.setText(titles[i]);
		}
	}

    private void updateCodeSnippets(Spinner commandSpinner) {
		String hexString = hexCodeInput.getText().toString().replace(" ", "");
		String asmCode = hexCodeInput.getText().toString();
		String selectedCommand = commandSpinner.getSelectedItem().toString();

		if ("Hex => Assembly".equals(selectedCommand)) {
			String[] results = convertHexToArmFormats(hexString);
			saveResults(results);
			displayResults(results);
		} else if ("Assembly => Hex".equals(selectedCommand)) {
			String[] results = convertAsmToHex(asmCode);
			saveResults(results);
			displayResults(results);
		}
    }

    private void displayResults(String[] results) {
		if (results != null && results.length > 0) {
			for (int i = 0; i < indexMapping.length; i++) {
				View box = findViewById(boxIds[i]);
				final TextView codeSnippet = box.findViewById(R.id.codeSnippet);
				ImageButton copyButton = box.findViewById(R.id.copyButton);
				copyButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							String textToCopy = codeSnippet.getText().toString();
							copyToClipboard(textToCopy);
						}
					});
				if (indexMapping[i] < results.length) {
					codeSnippet.setText(results[indexMapping[i]]);
				} else {
					codeSnippet.setText("");
				}
			}
		}
    }

    private void saveResults(String[] results) {
        if (results != null) {
			for (int i = 0; i < results.length && i < boxIds.length; i++) {
				editor.putString("result" + i, results[i]);
			}
			editor.apply();
        }
        String hexInputText = hexCodeInput.getText().toString();
        editor.putString("hexCodeInput", hexInputText);
        editor.apply();
    }

    private void saveSelectedCommand(String selectedCommand) {
        editor.putString("selectedCommand", selectedCommand);
        editor.apply();
    }

    private void loadPreferences(Spinner commandSpinner) {
        String savedHexInput = sharedPreferences.getString("hexCodeInput", "");
        hexCodeInput.setText(savedHexInput);
        for (int i = 0; i < boxIds.length; i++) {
			String savedResult = sharedPreferences.getString("result" + i, "");
			View box = findViewById(boxIds[i]);
			TextView codeSnippet = box.findViewById(R.id.codeSnippet);
			codeSnippet.setText(savedResult);
        }
        String savedCommand = sharedPreferences.getString("selectedCommand", "Hex => Assembly");
        int position = ((android.widget.ArrayAdapter) commandSpinner.getAdapter()).getPosition(savedCommand);
        commandSpinner.setSelection(position);
    }

    private boolean isHexFormat(String inputText) {
        return inputText.matches("([0-9A-Fa-f]{2}\\s)*[0-9A-Fa-f]{2}");
    }

    private void copyToClipboard(String text) {
		if (!text.isEmpty()) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboard != null) {
				clipboard.setPrimaryClip(ClipData.newPlainText("Code Snippet", text));
				MyToast(getApplicationContext(), "Copied to clipboard!");
			}
		}
    }

    private void pasteFromClipboard() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if (clipboard != null && clipboard.hasPrimaryClip()) {
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			if (item.getText() != null) {
				hexCodeInput.setText(item.getText().toString());
				MyToast(getApplicationContext(), "Pasted from clipboard!");
			}
		}
    }

    private static void MyToast(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(15);
        textView.setTextColor(Color.WHITE);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#FF4B4B4B"));
        gd.setCornerRadii(new float[]{20, 20, 30, 20, 20, 20, 20, 20});
        gd.setStroke(4, Color.WHITE);
        textView.setBackground(gd);
        Toast toast = Toast.makeText(context, null, Toast.LENGTH_LONG);
        toast.setView(textView);
        toast.show();
    }

	
	
	}

	
