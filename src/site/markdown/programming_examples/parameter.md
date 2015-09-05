#Parameter and Annotations

You can easily pass parameters from the xml file to a processor without much overhead.
Using the usual [JavaBeans convention](http://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions) you simply
create a private field with a given name and add getter and setter methods. Then you can simply pass parameters to your processor 
by adding them to the line in the .xml file.

The `@Parameter(required = true/false)` annotation will create some automated checks for your xml Parameters.
You can add the annotation to every field in your processor. In case the required flag is set to true, your .xml
file will be automatically checked for the parameter with the name of the field you annotated.

Here's a short example demonstrating how to pass a parameter ( in this case a float) to a processor.

        package fact.somePackage;

        //imports ...

        /**
         * Documentation goes here
         */
        public class SomeProcessor implements Processor {
        	//a private field with a default value
			@Parameter(required = true, description = "A nice description for this value.", defaultValue = "3")
			private float someValue = 3;

			...
			//process methods here
			...


			//some documentation for the parameter
			public void setSomeValue(float someValue) {
				this.someValue = someValue;
			}

		}

The usual convention here is to put the setter methods as last members in the class. The corresponding line
in the .xml file would look like this:

		<fact.somePackage.SomeProcessor someValue="3.141452234" />

If you don't set the someValue parameter in the xml and the `required`  flag is set to true you will get an error message.