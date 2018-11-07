var fs = require('fs');

all_words = [null, null];
stop_words = [null, null];
non_stop_words = [null, function(){ return all_words[0].map(function(w){ return stop_words[0].includes(w) ? null : w;});}];
unique_words = [null, function(){return Array.from(new Set(non_stop_words[0].filter(function(w){return w != null;})));}];
counts = [null, function(){return unique_words[0].map(function(word){ return non_stop_words[0].filter(function(w){return w==word;}).length;});}];
sorted_data = [null, function(){ return unique_words[0].map(function(w,i){return [w,counts[0][i]];}).sort(function(a,b){return b[1]-a[1];});}];
all_columns = [all_words,stop_words,non_stop_words,unique_words,counts,sorted_data];

function update(){
	for(i in all_columns){
		if(all_columns[i][1] != null)
			all_columns[i][0] = all_columns[i][1]();
	}
}

all_words[0] = fs.readFileSync(process.argv[2], 'utf8').replace(/[^0-9a-zA-Z]/gi, ' ').toLowerCase().match(/\S+/g);
stop_words[0] = fs.readFileSync('../stop_words.txt', 'utf8').split(',');
for(var i=0; i<26; i++)
	stop_words[0].push(String.fromCharCode('a'.charCodeAt(0)+i));
update()
for(i in sorted_data[0].slice(0, 25)){
	console.log(sorted_data[0][i][0] + " - " + sorted_data[0][i][1]);
}

