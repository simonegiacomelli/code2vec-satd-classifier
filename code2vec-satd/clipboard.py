def clipboard(content):
    try:
        from Tkinter import Tk
    except ImportError:
        from tkinter import Tk
    r = Tk()
    r.withdraw()
    r.clipboard_clear()
    r.clipboard_append(content)
    r.update()  # now it stays on the clipboard after the window is closed
    r.destroy()