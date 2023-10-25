import xlrd3 as x
import csv
import os
path = 'C:/Users/19616/Desktop/2023年9月/'
files = os.listdir(path, )
num = 0
with open(path + '/gs-2023-09.csv', 'w', newline='',encoding='utf-8-sig') as file:
    writer = csv.writer(file)
    for i in range(len(files)):
        if files[i][-4:] in ['xlsx']:
            print(f"开始处理文件[{i}/{len(files)}]：{files[i]}")
            wb = x.open_workbook(path + files[i])
            st = wb.sheet_by_index(0)
            print(f"获取数据调试：{st.nrows}")
            num = num + st.nrows-2
            for r in range(st.nrows):
                if (r > 1):
                    row = st.row_values(r)
                    writer.writerow(row)
print(f"数据处理完成，总数：{num}")