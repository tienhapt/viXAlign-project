/* 
 * @(#)       Main.java
 * 
 * Created    Tue Jan 14 14:55:56 2003
 * 
 * Copyright  2003 (C) Nguyen Thi Minh Huyen
 *            UMR LORIA (Universities of Nancy, CNRS & INRIA)
 *            
 */

package fr.loria.xsilfide.DblAlign;

import java.io.*;

import javax.swing.*;


import java.awt.*;
import java.awt.event.*;


/**
 * This is the main class for the alignment tool using KVec algorithm.
 * 
 * 
 * @author    Nguyen Thi Minh Huyen
 * @version   
 */

public class Main extends javax.swing.JFrame {

	public static String FileSeparator = System.getProperty("file.separator");

	int height, width;
	//JPanel sourcePanel, targetPanel, commonPanel;
	JTextField srcFile, srcPropFile, srcIgnoreFile, tarFile, tarPropFile, tarIgnoreFile, resFile;
	Skins skins;
	JCheckBox srcTokenized, tarTokenized;

	public Main(String skin,  // skin file name
			String param  // parameters file name
	) {
		skins = new Skins("Desktop", skin); // Load interface's labels
		Parameters.init(param);
		// Setting general properties of desktop
		this.setTitle(skins.getLabelOf("title"));
		height = skins.getValueOf("hsize");
		width = skins.getValueOf("wsize");
		this.setSize(new Dimension(width,height));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width-getWidth())/2,(screenSize.height - getHeight())/2);

		// Desktop : twosidePanel [sourcePanel, targetPanel] - commonPanel

		// Source Panel : Source File Chooser, Properties File Chooser, Tokenized check box, Language Text field
		JPanel sourcePanel = new JPanel(new GridLayout(5, 1));

		// source file chooser
		JLabel srcFileLabel = new JLabel(skins.getLabelOf("fileName"));
		srcFile = new JTextField("", 20);
		JButton srcBrowse = new JButton(skins.getLabelOf("browse"));
		srcBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("xml", "XML files (*.xml)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					srcFile.setText(path);
				}
			}
		});
		JPanel srcFileChooser = new JPanel(new FlowLayout());
		srcFileChooser.add(srcFileLabel);
		srcFileChooser.add(srcFile);
		srcFileChooser.add(srcBrowse);

		// source properties file chooser
		JLabel srcPropFileLabel = new JLabel(skins.getLabelOf("propFile"));
		srcPropFile = new JTextField(System.getProperty("user.dir")+ FileSeparator +"Properties" + FileSeparator + "multialign.properties", 20);
		JButton srcPropBrowse = new JButton(skins.getLabelOf("browse"));
		srcPropBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("properties", "properties files (*.properties)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					srcPropFile.setText(path);
				}
			}
		});
		JPanel srcPropFileChooser = new JPanel(new FlowLayout());
		srcPropFileChooser.add(srcPropFileLabel);
		srcPropFileChooser.add(srcPropFile);
		srcPropFileChooser.add(srcPropBrowse);

		// source ignored word file chooser
		JLabel srcIgnoreFileLabel = new JLabel(skins.getLabelOf("ignoreFile"));
		srcIgnoreFile = new JTextField("", 20);
		JButton srcIgnoreBrowse = new JButton(skins.getLabelOf("browse"));
		srcIgnoreBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("xml", "xml files (*.xml)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					srcIgnoreFile.setText(path);
				}
			}
		});
		JPanel srcIgnoreFileChooser = new JPanel(new FlowLayout());
		srcIgnoreFileChooser.add(srcIgnoreFileLabel);
		srcIgnoreFileChooser.add(srcIgnoreFile);
		srcIgnoreFileChooser.add(srcIgnoreBrowse);

		// tokenized check box
		srcTokenized = new JCheckBox(skins.getLabelOf("tokenChkbox"), false);

		// Language precision
		JLabel srcLangLabel = new JLabel(skins.getLabelOf("language"));
		JTextField srcLanguage = new JTextField("FR", 6);
		JPanel srcLangPanel = new JPanel(new FlowLayout());
		srcLangPanel.add(srcLangLabel);
		srcLangPanel.add(srcLanguage);

		sourcePanel.add(srcFileChooser);
		sourcePanel.add(srcPropFileChooser);
		sourcePanel.add(srcIgnoreFileChooser);
		sourcePanel.add(srcTokenized);
		sourcePanel.add(srcLangPanel);
		sourcePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(skins.getLabelOf("sourceLabel")),
				BorderFactory.createEmptyBorder(5,5,5,5)));


		// Target Panel : Target File Chooser, Tokenized check box, Language Text field
		JPanel targetPanel = new JPanel(new GridLayout(5, 1));

		// target file chooser
		JLabel tarFileLabel = new JLabel(skins.getLabelOf("fileName"));
		tarFile = new JTextField("", 20);
		JButton tarBrowse = new JButton(skins.getLabelOf("browse"));
		tarBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("xml", "XML files (*.xml)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					tarFile.setText(path);
				}
			}
		});
		JPanel tarFileChooser = new JPanel(new FlowLayout());
		tarFileChooser.add(tarFileLabel);
		tarFileChooser.add(tarFile);
		tarFileChooser.add(tarBrowse);


		// target properties file chooser
		JLabel tarPropFileLabel = new JLabel(skins.getLabelOf("propFile"));
		tarPropFile = new JTextField(System.getProperty("user.dir")+ FileSeparator +"Properties" + FileSeparator + "multialign.properties", 20);
		JButton tarPropBrowse = new JButton(skins.getLabelOf("browse"));
		tarPropBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("properties", "properties files (*.properties)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					tarPropFile.setText(path);
				}
			}
		});
		JPanel tarPropFileChooser = new JPanel(new FlowLayout());
		tarPropFileChooser.add(tarPropFileLabel);
		tarPropFileChooser.add(tarPropFile);
		tarPropFileChooser.add(tarPropBrowse);

		// target ignored word file chooser
		JLabel tarIgnoreFileLabel = new JLabel(skins.getLabelOf("ignoreFile"));
		tarIgnoreFile = new JTextField("", 20);
		JButton tarIgnoreBrowse = new JButton(skins.getLabelOf("browse"));
		tarIgnoreBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("xml", "xml files (*.xml)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					tarIgnoreFile.setText(path);
				}
			}
		});
		JPanel tarIgnoreFileChooser = new JPanel(new FlowLayout());
		tarIgnoreFileChooser.add(tarIgnoreFileLabel);
		tarIgnoreFileChooser.add(tarIgnoreFile);
		tarIgnoreFileChooser.add(tarIgnoreBrowse);

		// tokenized check box
		tarTokenized = new JCheckBox(skins.getLabelOf("tokenChkbox"), false);

		// Language precision
		JLabel tarLangLabel = new JLabel(skins.getLabelOf("language"));
		JTextField tarLanguage = new JTextField("FR", 4);
		JPanel tarLangPanel = new JPanel(new FlowLayout());
		tarLangPanel.add(tarLangLabel);
		tarLangPanel.add(tarLanguage);

		targetPanel.add(tarFileChooser);
		targetPanel.add(tarPropFileChooser);
		targetPanel.add(tarIgnoreFileChooser);
		targetPanel.add(tarTokenized);
		targetPanel.add(tarLangPanel);
		targetPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(skins.getLabelOf("targetLabel")),
				BorderFactory.createEmptyBorder(5,5,5,5)));

		JPanel twosidePanel = new JPanel(new GridLayout(1, 2));
		twosidePanel.add(sourcePanel);
		twosidePanel.add(targetPanel);

		// Common Panel: ResultFileChooser, Run button
		JPanel commonPanel = new JPanel(new GridLayout(2, 1));

		// ResultFileChooser
		JLabel resFileLabel = new JLabel(skins.getLabelOf("out"));
		resFile = new JTextField(System.getProperty("user.dir")+ FileSeparator +"out.txt", 20);
		JButton resBrowse = new JButton(skins.getLabelOf("browse"));
		resBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyFileFilter filter = new MyFileFilter("txt", "Text files (*.txt)");
				String path=selectFile(skins.getLabelOf("openFile"), filter);
				if (path!=null) {
					resFile.setText(path);
				}
			}
		});

		JPanel resFileChooser = new JPanel(new FlowLayout());
		resFileChooser.add(resFileLabel);
		resFileChooser.add(resFile);
		resFileChooser.add(resBrowse);

		JPanel runParamPanel = new JPanel(new FlowLayout());
		// Reset button : reload parameters
		JButton resetParam = new JButton(skins.getLabelOf("resetParam"));
		resetParam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Parameters.reset();
			}
		});
		// Run button
		JButton run = new JButton(skins.getLabelOf("run"));
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean smarkup, tmarkup;
				String sfile, tfile, ps, pt, is, it, out;
				smarkup = srcTokenized.isSelected();
				tmarkup = tarTokenized.isSelected();
				sfile = srcFile.getText().trim();
				tfile = tarFile.getText().trim();
				out = resFile.getText().trim();
				ps = srcPropFile.getText().trim();
				pt = tarPropFile.getText().trim();
				is = srcIgnoreFile.getText().trim();
				it = tarIgnoreFile.getText().trim();
				if (sfile.length()>0 && tfile.length()>0 && ps.length()>0 && pt.length()>0 && out.length()>0 && is.length()>0 && it.length()>0)
					new DblAlign(smarkup, tmarkup, sfile, tfile, ps, pt, is, it, out);
			}
		});

		runParamPanel.add(resetParam);
		runParamPanel.add(run);

		commonPanel.add(resFileChooser);
		commonPanel.add(runParamPanel);

		getContentPane().add(twosidePanel, BorderLayout.CENTER);
		getContentPane().add(commonPanel, BorderLayout.SOUTH);

		//	setJMenuBar(menuBar());

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}

	public String selectFile(String dialogTitle, MyFileFilter filter) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(filter);
		chooser.setDialogTitle(dialogTitle);
		chooser.setFileHidingEnabled(true);
		chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			String path = chooser.getSelectedFile().getAbsolutePath();
			return path;
		}
		return null;
	}

	public static void main(String[] args ) {
		if (args.length != 10) {
			System.err.println("Usage : align [t|p] [t|p] srcText tarText srcProp tarProp srcStopList tarStopList params outFile.");
			System.out.println("aaa"+args.length);
                        System.exit(-1);
		}

		Parameters.init(args[8]);

		boolean smarkup = (args[0].equals("t"));
		boolean tmarkup = (args[1].equals("t"));
		try {
			new DblAlign(smarkup, tmarkup,  // true values if texts are segmented in lexical units
					args[2], args[3], // source text, target text
					args[4], args[5], // source XML properties, target XML properties
					args[6], args[7], // source and target XML files of ignored words
					args[9]  // result
			);
		}
		catch (Exception e) {			
			e.printStackTrace();
		}

		// 	int arg = 0;
		// 	String directory = System.getProperty("user.dir");
		// 	String language = "en";

		//  	while (arg+1 < args.length) {
		// 	    String opt = args[arg++];
		// 	    if (opt.compareTo("-d")==0)
		// 		directory = args[arg++];
		// 	    else if (opt.compareTo("-l")==0)
		// 		language = args[arg++];
		// 	}

		// 	String skin = directory + "/Properties/skins." + language + ".xml";
		// 	String param = directory + "/Properties/thresholds.xml";

		// 	Main aligner = new Main(skin, param);
		//         aligner.setVisible(true);
	}

}
