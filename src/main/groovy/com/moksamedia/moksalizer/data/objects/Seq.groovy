package com.moksamedia.moksalizer.data.objects


class Seq {

	int post = 0
	
	public int _nextPostNumber() {
		post +=1 
		this.save()
		(post - 1)
	}
	
	public static int nextPostNumber() {
		Seq.getOne()._nextPostNumber()
	}
	
}