package com.go.plugin.task.msbuild;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.plugin.api.task.TaskView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Extension
public class MSBuildTask implements Task {
	public static final String CUSTOMIZEMSBUILDPATH = "CustomizeMSBuildPath"; 
    public static final String MSBUILDPATH = "MSBuildPath";
    public static final String SOLUTIONFILE = "SolutionFile";
    public static final String PROPERTIES = "Properties";
    public static final String VERBOSITY = "Verbosity";
    public static final String SPECIFYTARGETS = "SpecifyTargets";
    public static final String TARGETS = "Targets";
    public static final String DETAILEDSUMMARY = "DetailedSummary";
    public static final String NOLOGO = "NoLogo";
    public static final String NOAUTORESPONSE = "NoAutoResponse";

    @Override
    public TaskConfig config() {
        TaskConfig config = new TaskConfig();
        config.addProperty(CUSTOMIZEMSBUILDPATH);
        config.addProperty(MSBUILDPATH);
        config.addProperty(SOLUTIONFILE);
        
        config.addProperty(PROPERTIES);
        config.addProperty(VERBOSITY);
        config.addProperty(SPECIFYTARGETS);
        config.addProperty(TARGETS);
        
        config.addProperty(DETAILEDSUMMARY);
        config.addProperty(NOLOGO);
        config.addProperty(NOAUTORESPONSE);
        return config;
    }

    @Override
    public TaskExecutor executor() {
        return new MSBuildTaskExecutor();
    }

    @Override
    public TaskView view() {
        TaskView taskView = new TaskView() {
            @Override
            public String displayValue() {
                return "MSBuild";
            }

            @Override
            public String template() {
                try {
                    return IOUtils.toString(getClass().getResourceAsStream("/resources/views/task.template.html"), "UTF-8");
                } catch (Exception e) {
                    return "Failed to find template: " + e.getMessage();
                }
            }
        };
        return taskView;
    }

    @Override
    public ValidationResult validate(TaskConfig configuration) {
        ValidationResult validationResult = new ValidationResult();
        
        String customizemsbuildpath = configuration.getValue(CUSTOMIZEMSBUILDPATH);
        if(customizemsbuildpath != null && customizemsbuildpath.equals("true")) {
        	String msbuildpath = configuration.getValue(MSBUILDPATH);
        	if(StringUtils.isBlank(msbuildpath)) {
        		validationResult.addError(new ValidationError(MSBUILDPATH, "Path to MSBuild.exe must be specified"));
        	}
        }
        
        String solutionfile = configuration.getValue(SOLUTIONFILE);
        if (StringUtils.isBlank(solutionfile)) {
            validationResult.addError(new ValidationError(SOLUTIONFILE, "A Solution file must be specified"));
        }
        
        String properties = configuration.getValue(PROPERTIES);
        if (!StringUtils.isBlank(properties) && !propertiesValid(properties)) {
        	validationResult.addError(new ValidationError(PROPERTIES, "Invalid entry for Properties - make sure one property per line formatted like propName=propValue"));
        }
        
        String specifyTargets = configuration.getValue(SPECIFYTARGETS);
        if(specifyTargets.equals("true")) {
        	String targets = configuration.getValue(TARGETS);
        	if(StringUtils.isBlank(targets)) {
        		validationResult.addError(new ValidationError(TARGETS, "If SpecifyTargets is checked, Targets cannot be empty."));
        	}
        }
        
        return validationResult;
    }
    
    protected boolean propertiesValid(String properties){
    	//split properties on newline
    	String props[] = properties.split("\\r?\\n");
    	Pattern regex = Pattern.compile("\\w+=\\w+");
    	for(String prop : props) {
    		//strip all whitespace
    		prop = prop.replaceAll("\\s", ""); 
    		Matcher matcher = regex.matcher(prop);
    		
    		if(!matcher.matches()) 
    			return false;    	
    	}
    	return true;
    }
}