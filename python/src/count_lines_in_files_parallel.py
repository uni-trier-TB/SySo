import os
import re
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed

def count_lines(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    return line_count

def count_lines_in_all_files(folder_path, regex_str, threads):
    total_lines = 0
    regex = re.compile(regex_str)
    file_paths = []

    # Collect all file paths that match the regex
    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                file_paths.append(os.path.join(root, file_name))
    
    # Use ThreadPoolExecutor to count lines in each file in parallel
    with ThreadPoolExecutor(max_workers=threads) as executor:
        future_to_file = {executor.submit(count_lines, file_path): file_path for file_path in file_paths}
        
        for future in as_completed(future_to_file):
            total_lines += future.result()
    
    return total_lines

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Count lines in all files matching regex in a directory tree.')
    parser.add_argument('folder_path', help='Path to the folder')
    parser.add_argument('regex_str', help='Regular expression string to match files')
    parser.add_argument('--threads', type=int, default=4, help='Number of threads to use')
    
    args = parser.parse_args()
    total_lines = count_lines_in_all_files(args.folder_path, args.regex_str, args.threads)
    print(f'Total number of lines: {total_lines}')

