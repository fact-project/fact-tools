##Parameter
You can easily pass parameters from the xml file to a processor without much overhead.
Using the usual [JavaBeans convention](http://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions) you simply
create a private field with a given name and add getter and setter methods. Then you can simply pass parameters to your processor 
by adding them to the line in the .xml file.

Heres a short example demonstrating how to pass a parameter ( in this case a float) to a processor.

        package fact.somePackage;

        //imports ...

        /**
         * Documentation goes here
         */
        public class SomeProcessor implements Processor {
        	//a private field with a default value
			private float someValue = 3;	
			
			...
			//process methods here
			...

			// Getter and Setter//
			public float getSomeValue() {
				return someValue;
			}
			//documentation for the parameter
			@Parameter(required = false, description = "A nice description for this value.", defaultValue = "3")
			public void setSomeValue(float someValue) {
				this.someValue = someValue;
			}

		}

The usual convention here is to put the getter and setter methods as last members in the class. The correspnding line in the .xml file would look like this:

		<fact.somePackage.SomeProcessor someValue="3.141452234" />