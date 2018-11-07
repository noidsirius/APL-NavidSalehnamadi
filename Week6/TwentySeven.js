var fs = require('fs');


function *readLine(path_to_file){
	for(var line of fs.readFileSync(path_to_file, 'utf8').split('\n'))
		yield line;

}

function *readWord(path_to_file){
	for(var line of readLine(path_to_file)){
		var correct_line =line.replace(/[^0-9a-zA-Z]/gi, ' ');
		var lower_case_line = correct_line.toLowerCase();
		var words_of_lines = lower_case_line.match(/\S+/g);
		if(words_of_lines)
			for(var word of words_of_lines) 
				yield word;
	}
}
function *getNonStopWords(path_to_file){
	var stop_words = fs.readFileSync('../stop_words.txt', 'utf8').split(',');
	for(var i=0; i<26; i++)
		stop_words.push(String.fromCharCode('a'.charCodeAt(0)+i));
	for(var word of readWord(path_to_file))
		if(!stop_words.includes(word))
			yield word;
}

function *getWordFrequency(path_to_file){
	var wf = {}
	var counter = 0;
	for(var word of getNonStopWords(path_to_file)){
		wf[word] = wf[word] ? wf[word]+1 : 1;
		counter += 1
		if(counter % 5000 == 0)
			yield Object.keys(wf).map(function(key) { return [key, wf[key]]}).sort(function(a,b){ return b[1]-a[1];});
	}	
	yield Object.keys(wf).map(function(key) { return [key, wf[key]]}).sort(function(a,b){ return b[1]-a[1];});
}


for(var wf of getWordFrequency(process.argv[2])){
	console.log("--------------");
	for(var x of wf.slice(0, 25)){
		console.log(x[0] + " - " + x[1]);
	}
}


