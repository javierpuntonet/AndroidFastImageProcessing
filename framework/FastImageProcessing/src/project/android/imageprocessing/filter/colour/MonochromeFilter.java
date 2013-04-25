package project.android.imageprocessing.filter.colour;

import android.opengl.GLES20;
import project.android.imageprocessing.filter.BasicFilter;

public class MonochromeFilter extends BasicFilter {
	private static final String UNIFORM_INTENSITY = "u_Intensity";
	private static final String UNIFORM_COLOUR = "u_Colour";
	
	private int intensityHandle;
	private int colourHandle;
	private float intensity;
	private float[] colour;
	
	public MonochromeFilter(float[] colour, float intensity) {
		this.intensity = intensity;
		this.colour = colour;
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		intensityHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_INTENSITY);
		colourHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_COLOUR); 
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform1f(intensityHandle, intensity);
		GLES20.glUniform3f(colourHandle, colour[0], colour[1], colour[2]);
	}
	@Override
	protected String getFragmentShader() {
		return 
				"precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform float "+UNIFORM_INTENSITY+";\n"	
				+"uniform vec3 "+UNIFORM_COLOUR+";\n"	
				+"vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n"
				
		  		+"void main(){\n"
		  		+"   vec4 color = texture2D("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+");\n"	
				+"   float luminance =  dot(color.rgb, luminanceWeighting);\n"
		  		+"   vec4 desat = vec4(vec3(luminance), 1.0);\n"
				+"   vec4 outputColour = vec4(\n"
		  		+"     (desat.r < 0.5 ? (2.0 * desat.r * "+UNIFORM_COLOUR+".r) : (1.0 - 2.0 * (1.0 - desat.r) * (1.0 - "+UNIFORM_COLOUR+".r))),\n"
		  		+"     (desat.g < 0.5 ? (2.0 * desat.g * "+UNIFORM_COLOUR+".g) : (1.0 - 2.0 * (1.0 - desat.g) * (1.0 - "+UNIFORM_COLOUR+".g))),\n"
		  		+"     (desat.b < 0.5 ? (2.0 * desat.b * "+UNIFORM_COLOUR+".b) : (1.0 - 2.0 * (1.0 - desat.b) * (1.0 - "+UNIFORM_COLOUR+".b))),\n"
				+"     1.0);\n"
		  		+"   gl_FragColor = vec4(mix(color.rgb, outputColour.rgb, "+UNIFORM_INTENSITY+"), color.a);\n"
		  		+"}\n";	
	}
}