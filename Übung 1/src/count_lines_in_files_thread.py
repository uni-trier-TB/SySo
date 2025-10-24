import os
import threading
import time

def count_lines(file_path):
    """Zählt die Zeilen in einer einzelnen Datei."""
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    return line_count


def count_lines_in_all_files(folder_path, num_threads):
    """Zählt die Zeilen in allen Dateien im Ordner parallel mit einer angegebenen Anzahl an Threads."""
    total_lines = 0
    files = []

    for root, dirs, files_list in os.walk(folder_path):
        for file_name in files_list:
            file_path = os.path.join(root, file_name)
            files.append(file_path)

    lock = threading.Lock()

    def thread_function(file_path):
        nonlocal total_lines
        file_lines = count_lines(file_path)
        with lock:
            total_lines += file_lines


    threads = []
    for i in range(0, len(files), num_threads):

        for file_path in files[i:i+num_threads]:
            thread = threading.Thread(target=thread_function, args=(file_path,))
            threads.append(thread)
            thread.start()

        for thread in threads:
            thread.join()

    return total_lines


if __name__ == "__main__":
    start_time = time.time()

    folder_path = r"C:\Users\Tony\PycharmProjects\SySo"
    num_threads = 4

    total_lines = count_lines_in_all_files(folder_path, num_threads)
    print(f'Total number of lines in all files: {total_lines}')
    end_time = time.time()
    elapsed_time = end_time - start_time
    print(elapsed_time)
    print("Number of Threads: ", num_threads)

