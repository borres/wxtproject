		/**
		* Cleans a string from everything but letters and digits.
		* Others are replaced by _
		* Removes Schema, in case it is an uri
		* @param S The string to clean, may be a simple string or an URI
		* @return The cleaned String
		*/
		public static String cleanStringForUseAsFilePath(String S)
		{
			 S=S.replace("http://", "");
			 S=S.replace("file://", "");

			 StringBuffer result=new StringBuffer(50);
			 for(int ix=0;ix<S.length();ix++)
			 {
				 char c=S.charAt(ix);
				 if( (!Character.isDigit(c)) && 
					 (!Character.isLetter(c))&&
					 (c!='.'))
						 result.append('_');
				 else
					 result.append(c);
			 }
			 String s=result.toString();
			 return s;
		}
