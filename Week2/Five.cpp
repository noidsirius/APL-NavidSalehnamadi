#include<iostream>
#include<fstream>
#include<vector>
#include<map>
#include<set>
#include<sstream>
#include<algorithm>
#include<cstring>
using namespace std;


// General Helper Functions
bool isBlank(string word){
	for(int i=0; i<word.length(); i++)
		if(!isspace(word[i]))
			return false;
	return true;
}

bool sortbysec(const pair<string,int> &a, const pair<string,int> &b) 
{ 
    return (a.second > b.second); 
} 

// Primary procedures

string get_whole_text(char *argv[]){
	ifstream text_file;
	text_file.open(argv[1]);
	if(text_file.fail()){
		cerr <<"This file is not accessible" << endl;
		exit(1);
	}
	string line;
	string text(argv[2]);
	text += '\n';
	while(getline(text_file, line))
		text += line + '\n';
	text_file.close();
	return text;
}

string refine_text(string text){
	for(int i=text.find('\n')+1; i< text.length(); i++)
	    if(isalpha(text[i]) || isspace(text[i]) )
	        text[i] = tolower(text[i]);
	    else
	        text[i] = ' ';
	return text;
}

string remove_stop_words(string whole_text){
	string file_name = "";
	int file_name_length = 0;
	for(int i=0; i<whole_text.length(); i++)
		if (whole_text[i] == '\n'){
			file_name_length = i;
			break;
		}
		else
		    file_name += whole_text[i];
	whole_text = whole_text.substr(file_name_length+1);
	ifstream stop_words_file;
	stop_words_file.open(file_name.c_str());
	string word;
	set<string> stop_words;
	while(getline(stop_words_file, word, ',')){
		if(isBlank(word))
			continue;
		stop_words.insert(word);
	}
	stop_words_file.close();
	string new_text = "";
	stringstream ss(whole_text);
	while(ss >> word){
		if(stop_words.find(word) != stop_words.end())
			continue;
		if(isBlank(word) or word.length() == 1)
			continue;
		new_text += word + ' ';
	}
	return new_text;
}

vector<pair<string, int> > find_and_sort_frequencies(string whole_text){
	string word;
	map<string, int> frequency_words;
	stringstream ss(whole_text);
	while(ss >> word){
		if(frequency_words.find(word) == frequency_words.end())
			frequency_words[word] = 1;
		else
			frequency_words[word] = frequency_words[word] + 1;
	}
	vector <pair<string, int> > term_frequencies;
	for(map<string,int>::iterator it = frequency_words.begin(); it != frequency_words.end(); it++)
		term_frequencies.push_back(make_pair(it->first, it->second));
	sort(term_frequencies.begin(), term_frequencies.end(), sortbysec);
	return term_frequencies;
}

void print_frequencies(vector<pair<string, int> > term_frequencies){
	for(int i=0; i<25; i++)
		cout << term_frequencies[i].first << "  -   " << term_frequencies[i].second << endl;	
}

int main(int argc, char *argv[]){
	if(argc != 3)
	{
		cerr << "Not enough arguments" << endl;
		return 1;
	}
	print_frequencies(find_and_sort_frequencies(remove_stop_words(refine_text(get_whole_text(argv)))));
	return 0;
}
