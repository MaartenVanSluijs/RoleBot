package slashCommands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigLoader {
	
	private Config conf;
	
	public Config getConf() {
		return conf;
	}

	public Config loadConfig()
	{
		Yaml yaml = new Yaml(new Constructor(Config.class));
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(new File("config.yml"));
		} catch (FileNotFoundException e) {
			inputStream = this.getClass().getClassLoader().getResourceAsStream("config.yml");
			try {
				FileOutputStream fos = new FileOutputStream(new File("config.yml"));
				byte[] buffer = new byte[inputStream.available()];
			    inputStream.read(buffer);
			    fos.write(buffer);
			    fos.close();
			} catch (FileNotFoundException e1) {
				System.out.println("Could not write default config.yml to drive.");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		conf = yaml.load(inputStream);
		try {
			inputStream.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(conf == null)
		{
			System.out.println("config is null");
			System.exit(0);
		}
		
		if(conf.getSqliteDatabase().equals("changeme"))
		{
			System.out.println("Please change the values in config.yml to your needs and restart the application.");
			System.exit(0);
		}
		
		
		return conf;
	}
	
	
	

}
