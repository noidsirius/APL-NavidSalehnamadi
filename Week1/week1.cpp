#include<iostream>
#include<fstream>
#include<vector>
#include<map>
#include<set>
#include<sstream>
using namespace std;

bool isBlank(string word){
	for(int i=0; i<word.length(); i++)
		if(!isspace(word[i]))
			return false;
	return true;
}

set<string> stop_words;

void import_stop_words(){
	ifstream stop_words_file;
	stop_words_file.open("../stop_words.txt");
	string word;
	while(getline(stop_words_file, word, ',')){
		if(isBlank(word))
			continue;
		stop_words.insert(word);
	}
	stop_words_file.close();
}

string remove_extra_chars(string word){
	string new_word = "";
	for(int i=0; i< word.length(); i++)
		if(isalpha(word[i]) || isspace(word[i]) || word[i] == '\'')
			new_word += tolower(word[i]);
		else
			new_word += ' ';
	return new_word;
}

string refine_text(string whole_text){
	string new_whole_text = "";
	stringstream ss(whole_text);
	string word;
	while(ss >> word){
		new_whole_text += remove_extra_chars(word) + ' ';
	}
	return new_whole_text;
}

bool sortbysec(const pair<string,int> &a, const pair<string,int> &b) 
{ 
    return (a.second > b.second); 
} 

int main(int argc, char *argv[]){
	if(argc != 2)
	{
		cerr << "Not enough arguments" << endl;
		return 1;
	}
	import_stop_words();
	ifstream text_file(argv[1]);
	if(text_file.fail()){
		cerr <<"This file is not accessible" << endl;
		return 1;
	}
	string whole_text = "", line;
	while(getline(text_file, line))
		whole_text += line + '\n';
	text_file.close();
	//cout << whole_text << endl;
	whole_text = refine_text(whole_text);
	string word;
	map<string, int> frequency_words;
	stringstream ss(whole_text);
	//cout << whole_text << endl;
	while(ss >> word){
		if(stop_words.find(word) != stop_words.end())
			continue;
		//cout << word << ' ';
		if(frequency_words.find(word) == frequency_words.end())
			frequency_words[word] = 1;
		else{
			frequency_words[word] = frequency_words[word] + 1;
		//	cout << word << ' ' << frequency_words[word] << endl;
		}
	}
	//cout << "SS" << endl;
	vector<pair<string, int> > final_words_frequency;
	for(map<string,int>::iterator it = frequency_words.begin(); it != frequency_words.end(); it++)
		final_words_frequency.push_back(make_pair(it->first, it->second));
	sort(final_words_frequency.begin(), final_words_frequency.end(), sortbysec);
	for(int i=0; i<25; i++)
		cout << final_words_frequency[i].first << "  -   " << final_words_frequency[i].second << endl;
	return 0;
}
